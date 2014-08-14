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
package org.openmrs.module.operationtheater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import java.util.Date;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class OperationTheaterModuleActivator implements ModuleActivator, DaemonTokenAware {

	public static DaemonToken DAEMON_TOKEN;

	protected Log log = LogFactory.getLog(getClass());

	private IdentifierSourceService idService;

	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Operation Theater Module");
	}

	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Operation Theater Module refreshed");
	}

	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Operation Theater Module");
	}

	/**
	 * @should create emergency procedure and patient placeholder
	 * @see ModuleActivator#started()
	 */
	public void started() {
		log.info("Operation Theater Module started");

		OperationTheaterService otService = Context.getService(OperationTheaterService.class);
		PatientService patientService = Context.getPatientService();
		LocationService locationService = Context.getLocationService();
		if (idService == null) {
			idService = Context.getService(IdentifierSourceService.class);
		}

		setUpEmergencyPlaceholders(otService, patientService, locationService);
	}

	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Operation Theater Module");
	}

	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Operation Theater Module stopped");
	}

	@Override
	public void setDaemonToken(DaemonToken token) {
		this.DAEMON_TOKEN = token;
	}

	private void setUpEmergencyPlaceholders(OperationTheaterService otService,
	                                        PatientService patientService,
	                                        LocationService locationService) {

		//placeholder procedure
		if (otService.getProcedureByUuid(OTMetadata.PLACEHOLDER_PROCEDURE_UUID) == null) {
			Procedure procedure = getEmergencyProcedure();
			otService.saveProcedure(procedure);
		}

		//placeholder patient
		if (patientService.getPatientByUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID) == null) {
			PatientIdentifierType patientIdentifierType = patientService
					.getPatientIdentifierTypeByName(OTMetadata.OPENMRS_ID_NAME);
			Patient patient = getEmergencyPatient(patientIdentifierType, locationService);
			patientService.savePatient(patient);
		}

	}

	private Patient getEmergencyPatient(PatientIdentifierType patientIdentifierType, LocationService locationService) {
		Patient patient = new Patient();
		patient.setUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID);

		PersonName pName = new PersonName();
		String gender = "M";
		boolean male = gender.equals("M");
		pName.setGivenName("EMERGENCY");
		pName.setFamilyName("PLACEHOLDER PATIENT");
		patient.addName(pName);

		patient.setBirthdate(new DateTime(1970, 1, 1, 0, 0).toDate());
		patient.setBirthdateEstimated(false);
		patient.setGender(gender);

		PatientIdentifier pa1 = new PatientIdentifier();
		pa1.setIdentifier(idService.generateIdentifier(patientIdentifierType, "EmergencyData"));
		pa1.setIdentifierType(patientIdentifierType);
		pa1.setDateCreated(new Date());
		pa1.setLocation(locationService.getLocation(1));
		patient.addIdentifier(pa1);

		return patient;
	}

	private Procedure getEmergencyProcedure() {
		Procedure procedure = new Procedure();
		procedure.setUuid(OTMetadata.PLACEHOLDER_PROCEDURE_UUID);
		procedure.setName("EMERGENCY Placeholder");
		procedure.setDescription("This procedure is used as placeholder for emergencies");
		procedure.setOtPreparationDuration(10);
		procedure.setInterventionDuration(50);
		procedure.setInpatientStay(1);
		return procedure;
	}
}
