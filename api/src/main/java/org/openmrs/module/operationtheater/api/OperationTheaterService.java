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
package org.openmrs.module.operationtheater.api;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(OperationTheaterService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface OperationTheaterService extends OpenmrsService {

	/**
	 * Sets the DAO for this service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param dao DAO for this service
	 */
	public void setProcedureDAO(ProcedureDAO dao);

	/**
	 * Sets the DAO for this service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param dao DAO for this service
	 */
	public void setSurgeryDAO(SurgeryDAO dao);

	/**
	 * Sets the DAO for the Patient service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param dao DAO for this service
	 */
	public void setPatientService(PatientService dao);

	/**
	 * Creates or updates the given surgery in the database.
	 *
	 * @param surgery surgery to be created
	 * @throws APIException
	 * @should call surgeryDao saveOrUpdate
	 */
	public void saveSurgery(Surgery surgery) throws APIException;

	/**
	 * Creates or updates the given surgery in the database.
	 *
	 * @param procedure procedure to be created
	 * @throws org.openmrs.api.APIException
	 * @should call procedureDao saveOrUpdate
	 */
	public void saveProcedure(Procedure procedure) throws APIException;

	/**
	 * gets list of all Procedures in the database
	 *
	 * @return
	 * @throws org.openmrs.api.APIException
	 */
	public List<Procedure> getAllProcedures() throws APIException;
}
