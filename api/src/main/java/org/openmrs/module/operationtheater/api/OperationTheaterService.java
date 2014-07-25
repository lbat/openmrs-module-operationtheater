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

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
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
	 * @param patientService
	 */
	public void setPatientService(PatientService patientService);

	/**
	 * Sets the DAO for the Patient service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param appointmentService
	 */
	public void setAppointmentService(AppointmentService appointmentService);

	/**
	 * Creates or updates the given surgery in the database.
	 *
	 * @param surgery surgery to be created
	 * @return created or updated surgery
	 * @throws APIException
	 * @should validate surgery object and call surgeryDao saveOrUpdate
	 */
	public Surgery saveSurgery(Surgery surgery) throws APIException;

	/**
	 * Get all surgeries based on includeVoided flag
	 *
	 * @param includeVoided
	 * @return List of all time slots
	 * @should return result of surgeryDAO getAllData method with parameter includeVoided
	 */
	public List<Surgery> getAllSurgeries(boolean includeVoided);

	/**
	 * Get procedure with the specified uuid
	 *
	 * @param uuid
	 * @return procedure with given uuid
	 * @should call procedureDAO getByUuid
	 */
	public Procedure getProcedureByUuid(String uuid);

	/**
	 * Get procedure with the specified id
	 *
	 * @param id
	 * @return procedure with given id
	 * @should call procedureDAO getById
	 */
	public Procedure getProcedure(Integer id);

	/**
	 * Creates or updates the given surgery in the database.
	 *
	 * @param procedure procedure to be created
	 * @return created or updated procedure
	 * @throws org.openmrs.api.APIException
	 * @should validate procedure object and call procedureDao saveOrUpdate
	 */
	public Procedure saveProcedure(Procedure procedure) throws APIException;

	/**
	 * gets list of all Procedures in the database
	 *
	 * @param includeRetired
	 * @return
	 * @throws org.openmrs.api.APIException
	 * @should return result of procedureDAO getAll method with parameter includeRetired
	 */
	public List<Procedure> getAllProcedures(boolean includeRetired) throws APIException;

	/**
	 * Get the surgery with the specified uuid
	 *
	 * @param uuid
	 * @return surgery with given uuid
	 * @should call surgeryDAO getByUuid
	 */
	public Surgery getSurgeryByUuid(String uuid);

	/**
	 * void the given surgery
	 *
	 * @param surgery
	 * @param reason  for the voiding
	 * @should call surgeryDAO saveOrUpdate if object is not null
	 * @should void the given surgery TODO add context sensitive test to see if aop kicks in
	 */
	public Surgery voidSurgery(Surgery surgery, String reason);

	/**
	 * unvoid the given surgery
	 *
	 * @param surgery
	 * @return
	 * @should unvoid the given surgery
	 */
	public Surgery unvoidSurgery(Surgery surgery);

	/**
	 * get all unvoided surgeries that are associated with the given patient
	 *
	 * @param patient
	 * @return all unvoided surgeries that are associated with the given patient
	 * @should call surgeryDAO getSurgeriesByPatient
	 * @should do nothing if patient is null
	 */
	public List<Surgery> getSurgeriesByPatient(Patient patient);

	/**
	 * retire the given procedure
	 *
	 * @param procedureToRetire
	 * @param reason
	 * @should call procedureDAO saveOrUpdate if object is not null
	 * @should retire the given procedure TODO add context sensitive test to see if aop kicks in
	 */
	public Procedure retireProcedure(Procedure procedureToRetire, String reason);

	/**
	 * Get Surgery with the specified id
	 *
	 * @param id
	 * @return surgery with given id
	 * @should call surgeryDAO getById
	 */
	public Surgery getSurgery(Integer id);

	/**
	 * get all uncompleted surgeries
	 *
	 * @return all uncompleted surgeries
	 * @should call surgeryDAO getAllUncompletedSurgeries
	 */
	public List<Surgery> getAllUncompletedSurgeries();

	/**
	 * get all unvoided surgeries that are scheduled between from and to
	 *
	 * @param from
	 * @param to
	 * @return
	 * @should call surgeryDAO getScheduledSurgeries if parameter are not null
	 * @should return empty list if a parameter is null
	 */
	public List<Surgery> getScheduledSurgeries(DateTime from, DateTime to);

	/**
	 * get available times for the given date and location
	 * custom available times for specific dates are stored inside appointment blocks,
	 * defaults are stored as location attributes
	 *
	 * @param date
	 * @return
	 * @should return interval from corresponding appointmentBlock
	 * @should return interval from location attribute if there is no appointmentBlock entry
	 * @should throw APIException if there is more than one appoitnmentBlock for this day and location
	 * @should throw APIException if availableStart attribute is not defined
	 * @should throw APIException if availableEnd attribute is not defined
	 */
	public Interval getLocationAvailableTime(Location location, DateTime date);
}
