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

//import org.openmrs.module.coreapps.contextmodel.VisitContextModel;

public class SurgeryPageController {

	//	public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
	//	                         @InjectBeans PatientDomainWrapper patientDomainWrapper,
	//	                         @SpringBean("adtService") AdtService adtService,
	//	                         @SpringBean("visitService") VisitService visitService,
	//	                         @SpringBean("encounterService") EncounterService encounterService,
	//	                         @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
	//	                         @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	//	                         @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	//	                         UiSessionContext sessionContext) {
	//
	//		if (patient.isVoided() || patient.isPersonVoided()) {
	//			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
	//		}
	//
	//		patientDomainWrapper.setPatient(patient);
	//		model.addAttribute("patient", patientDomainWrapper);
	//
	//		Location visitLocation = null;
	//		try {
	//			visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
	//		}
	//		catch (IllegalArgumentException ex) {
	//			// location does not support visits
	//		}
	//
	//		VisitDomainWrapper activeVisit = null;
	//		if (visitLocation != null) {
	//			activeVisit = adtService.getActiveVisit(patient, visitLocation);
	//		}
	//		model.addAttribute("activeVisit", activeVisit);
	//
	//		AppContextModel contextModel = sessionContext.generateAppContextModel();
	//		contextModel.put("patientId", patient.getId());
	//		contextModel.put("patientDead", patient.isDead());
	//		contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));
	//
	//		List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.overallActions", contextModel);
	//		Collections.sort(overallActions);
	//		model.addAttribute("overallActions", overallActions);
	//
	//		List<Extension> visitActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.visitActions", contextModel);
	//		Collections.sort(visitActions);
	//		model.addAttribute("visitActions", visitActions);
	//
	//		List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
	//		Collections.sort(includeFragments);
	//		model.addAttribute("includeFragments", includeFragments);
	//
	//		List<Extension> otherActions = appFrameworkService.getExtensionsForCurrentUser(
	//				"clinicianFacingPatientDashboard.otherActions", contextModel);
	//		Collections.sort(otherActions);
	//		model.addAttribute("otherActions", otherActions);
	//
	//		applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());
	//
	//		return null;
	//	}

	public void controller() {

	}

}
