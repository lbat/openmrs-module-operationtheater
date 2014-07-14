package org.openmrs.module.operationtheater.fragment.controller;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientsSurgeriesFragmentController {

	/**
	 * @param ui
	 * @param surgeryId
	 * @param service
	 * @return
	 * @should retire surgery and return success result
	 * @should return failure result if no surgery with given primary key is found in the db
	 * @should return failure result if user is not allowed to retire a surgery
	 */
	public FragmentActionResult voidSurgery(UiUtils ui,
	                                        @RequestParam(value = "surgeryId", required = true) int surgeryId,
	                                        @SpringBean OperationTheaterService service) {

		Surgery surgeryToRetire = service.getSurgery(surgeryId);

		if (surgeryToRetire != null) {
			try {
				service.voidSurgery(surgeryToRetire,
						"");
				return new SuccessResult("operationtheater.surgery.voidedSuccessfully");
			}
			catch (APIAuthenticationException e) {
				return new FailureResult(ui.message("operationtheater.surgery.void.notAllowed"));
			}
		} else {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
	}
}
