/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEventHandler;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/

@Singleton
public class ValidationService implements RunValidationEventHandler, TransUnitSelectionHandler, DocumentSelectionHandler
{
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final ValidationMessages validationMessages;
   private Map<ValidationId, ValidationAction> validationMap;

   @Inject
   public ValidationService(final EventBus eventBus, final TableEditorMessages messages, final ValidationMessages validationMessages)
   {
      this.eventBus = eventBus;
      this.messages = messages;
      this.validationMessages = validationMessages;

      eventBus.addHandler(RunValidationEvent.getType(), this);
      eventBus.addHandler(TransUnitSelectionEvent.getType(), this);
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
   }

   @Override
   public void onValidate(RunValidationEvent event)
   {
      execute(event.getSourceContent(), event.getTarget(), event.isFireNotification(), event.getWidgetList());
   }

   @Override
   public void onTransUnitSelected(TransUnitSelectionEvent event)
   {
      clearAllMessage();
   }

   @Override
   public void onDocumentSelected(DocumentSelectionEvent event)
   {
      clearAllMessage();
   }

   /**
    * Run all enabled validators against the given strings. Generates a
    * {@link HasUpdateValidationWarning}, which will be empty if no warnings
    * were generated by the enabled validators.
    */
   public void execute(String source, String target, boolean fireNotification, ArrayList<HasUpdateValidationWarning> widgetList)
   {
      List<String> errors = new ArrayList<String>();

      for (ValidationAction validationAction : validationMap.values())
      {
         if (validationAction.getValidationInfo().isEnabled())
         {
            validationAction.clearErrorMessage();
            validationAction.validate(source, target);
            errors.addAll(validationAction.getError());
         }
      }
      fireValidationWarningsEvent(errors, fireNotification, widgetList);
   }

   /**
    * Enable/disable validation action. Causes a {@link RequestValidationEvent}
    * to be fired.
    *
    * @param key
    * @param isEnabled
    */
   public void updateStatus(ValidationId key, boolean isEnabled)
   {
      ValidationAction action = validationMap.get(key);
      action.getValidationInfo().setEnabled(isEnabled);

      // request re-run validation with new options
      eventBus.fireEvent(RequestValidationEvent.EVENT);
   }

   public Map<ValidationId, ValidationAction> getValidationMap()
   {
      return validationMap;
   }

   /**
    * Clear all validation plugin's error messages
    */
   public void clearAllMessage()
   {
      for (ValidationAction validationAction : validationMap.values())
      {
         validationAction.clearErrorMessage();
      }
   }

   public void fireValidationWarningsEvent(List<String> errors, boolean fireNotification, ArrayList<HasUpdateValidationWarning> widgetList)
   {
      if (!errors.isEmpty() && fireNotification)
      {
         eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyValidationError()));
      }

      if (widgetList != null)
      {
         for (HasUpdateValidationWarning widget : widgetList)
         {
            widget.updateValidationWarning(errors);
         }
      }
   }

   /**
    * Merge ValidationInfo from RPC result ValidationObject to all validation
    * actions from ValidationFactory
    * 
    * @param validations
    */
   public void setValidationRules(List<ValidationInfo> validationInfoList)
   {
      Map<ValidationId, ValidationAction> validationMap = ValidationFactory.getAllValidationActions(validationMessages);
      
      for (ValidationInfo valInfo : validationInfoList)
      {
         validationMap.get(valInfo.getId()).setValidationInfo(valInfo);
      }
      
      this.validationMap = validationMap;
   }
}
