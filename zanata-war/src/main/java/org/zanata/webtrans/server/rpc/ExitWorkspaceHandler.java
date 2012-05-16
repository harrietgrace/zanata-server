package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceResult;

@Name("webtrans.gwt.ExitWorkspaceHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ExitWorkspaceAction.class)
public class ExitWorkspaceHandler extends AbstractActionHandler<ExitWorkspaceAction, ExitWorkspaceResult>
{

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public ExitWorkspaceResult execute(ExitWorkspaceAction action, ExecutionContext context) throws ActionException
   {

      ZanataIdentity.instance().checkLoggedIn();

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      // Send ExitWorkspace event to client
      boolean isRemoved = workspace.removeTranslator(action.getSessionId(), action.getPerson().getId());
      if (isRemoved)
      {
         // Send GWT Event to client to update the userlist
         ExitWorkspace event = new ExitWorkspace(action.getSessionId(), action.getPerson());
         workspace.publish(event);
      }

      return new ExitWorkspaceResult(action.getPerson().getId().toString());
   }

   @Override
   public void rollback(ExitWorkspaceAction action, ExitWorkspaceResult result, ExecutionContext context) throws ActionException
   {
   }
}