/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.operationtheater.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.OperationTheaterDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * It is a default implementation of  {@link OperationTheaterDAO}.
 */
@Repository
public class HibernateSurgeryDAO extends HibernateGenericDAO<Surgery> implements SurgeryDAO {

	protected final Log log = LogFactory.getLog(this.getClass());

	public HibernateSurgeryDAO() {
		super(Surgery.class);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Surgery> getSurgeriesByPatient(Patient patient) {
		return super.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from " + mappedClass.getSimpleName()
								+ " at where at.patient = :patient and at.voided=false"
				)
				.setParameter("patient", patient).list();
	}

	@Override
	public List<Surgery> getAllUncompletedSurgeries() {
		return super.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from " + mappedClass.getSimpleName()
								+ " at where at.dateFinished = null and at.voided=false"
				).list();
	}

	@Override
	public List<Surgery> getScheduledSurgeries(DateTime from, DateTime to) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Surgery.class);
		criteria.add(Restrictions.eq("voided", false));

		criteria.createAlias("schedulingData", "schedulingData");

		if (from != null) {
			criteria.add(Restrictions.ge("schedulingData.start", from));
		}

		if (to != null) {
			criteria.add(Restrictions.le("schedulingData.end", to));
		}

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}
}
