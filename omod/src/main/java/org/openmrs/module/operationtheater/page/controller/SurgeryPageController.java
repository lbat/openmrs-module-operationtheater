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
package org.openmrs.module.operationtheater.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class SurgeryPageController {

	protected final Log log = LogFactory.getLog(getClass());

	public Object controller(PageModel model,
	                         @RequestParam(value = "patientId", required = true) Patient patient,
	                         @RequestParam(value = "surgeryId", required = false) Surgery surgery,
	                         @SpringBean OperationTheaterService otService,
	                         @SpringBean("providerService") ProviderService providerService,
	                         @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                         @SpringBean("adtService") AdtService adtService,
	                         @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	                         UiSessionContext sessionContext) {

		if (patient.isVoided() || patient.isPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
		}

		if (surgery == null) {
			surgery = new Surgery();
			surgery.setProcedure(new Procedure());
		}
		model.addAttribute("surgery", surgery);

		List<Procedure> procedureList = otService.getAllProcedures(false);
		model.addAttribute("procedureList", procedureList);

		//get list of providers and remove those that are already part of the surgical team
		List<Provider> providerList = providerService.getAllProviders();
		if (surgery.getSurgicalTeam() != null) {
			providerList.removeAll(surgery.getSurgicalTeam());
		}
		model.addAttribute("providerList", providerList);

		//is patient a emergency placeholder?
		boolean isPlaceholderPatient =
				surgery.getPatient() != null && surgery.getPatient().getUuid().equals(OTMetadata.PLACEHOLDER_PATIENT_UUID);
		model.addAttribute("emergencyPatient", isPlaceholderPatient);

		patientDomainWrapper.setPatient(patient);
		model.addAttribute("patient", patientDomainWrapper);

		Location visitLocation = null;
		try {
			visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
		}
		catch (IllegalArgumentException ex) {
			// location does not support visits
		}

		VisitDomainWrapper activeVisit = null;
		if (visitLocation != null) {
			activeVisit = adtService.getActiveVisit(patient, visitLocation);
		}
		model.addAttribute("activeVisit", activeVisit);

		AppContextModel contextModel = sessionContext.generateAppContextModel();
		contextModel.put("patientId", patient.getId());
		contextModel.put("patientDead", patient.isDead());

		applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

		return null;
	}
}
