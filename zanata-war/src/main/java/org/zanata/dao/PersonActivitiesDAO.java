/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.UserActionType;
import org.zanata.model.HPersonActivities;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("personActivitiesDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class PersonActivitiesDAO extends AbstractDAOImpl<HPersonActivities, Long>
{
   private static final long serialVersionUID = 1L;

   public PersonActivitiesDAO()
   {
      super(HPersonActivities.class);
   }

   public PersonActivitiesDAO(Session session)
   {
      super(HPersonActivities.class, session);
   }

   @SuppressWarnings("unchecked")
   public HPersonActivities findUserActivity(Long personId, Long versionId, UserActionType action)
   {
      Query query = getSession().createQuery("FROM HPersonActivities WHERE person.id = :personId "
            + "AND projectIteration.id = :versionId "
            + "AND action = :action");
      query.setParameter("personId", personId);
      query.setParameter("versionId", versionId);
      query.setParameter("action", action);
      query.setCacheable(true);
      query.setComment("PersonActivitiesDAO.findUserActivity");
      return (HPersonActivities)query.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HPersonActivities> findAllUserActivities(Long personId, Long versionId, int offset, int count)
   {
      Query query = getSession().createQuery("FROM HPersonActivities WHERE person.id = :personId "
            + "AND projectIteration.id = :versionId");
      query.setParameter("personId", personId);
      query.setParameter("versionId", versionId);
      query.setMaxResults(count);
      query.setFirstResult(offset);
      query.setCacheable(true);
      query.setComment("PersonActivitiesDAO.findAllUserActivities");
      return query.list();
   }
}
