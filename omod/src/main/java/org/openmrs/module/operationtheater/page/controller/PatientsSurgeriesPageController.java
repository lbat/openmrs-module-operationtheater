package org.openmrs.module.operationtheater.page.controller;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class PatientsSurgeriesPageController {

	public void get(PageModel model,
	                UiUtils ui,
	                @RequestParam(value = "patientId", required = true) Patient patient,
	                @RequestParam(value = "deleted", required = false) String procedureDeleted,
	                @SpringBean OperationTheaterService otService,
	                @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                @SpringBean("adtService") AdtService adtService,
	                @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	                UiSessionContext sessionContext) throws Exception {

		model.addAttribute("patientId", patient.getId());

		String resultMessage = "";
		if (procedureDeleted.equals("true")) {
			resultMessage = ui.message("operationtheater.manageprocedures.success");
		}
		model.addAttribute("resultMessage", resultMessage);

		List<Procedure> procedureList = otService.getAllProcedures(false);
		model.addAttribute("procedureList", procedureList);

		List<Surgery> surgeryList = otService.getSurgeriesByPatient(patient);
		model.addAttribute("surgeryList", surgeryList);

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
	}
}
