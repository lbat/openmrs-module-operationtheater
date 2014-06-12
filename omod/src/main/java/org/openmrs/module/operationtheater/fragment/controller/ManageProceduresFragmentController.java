package org.openmrs.module.operationtheater.fragment.controller;

import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

public class ManageProceduresFragmentController {

	public FragmentActionResult retireProcedure(UiUtils ui,
	                                            @RequestParam(value = "procedureId", required = true) int procedureId,
	                                            @SpringBean OperationTheaterService service) {

		Procedure procedureToRetire = service.getProcedure(procedureId);

		if (procedureToRetire != null) {
			service.retireProcedure(procedureToRetire,
					"Retired procedure by system administration");
			return new SuccessResult("deleted");
		} else {
			return new FailureResult(ui.message("operationtheater.manageprocedures.notAllowed"));
		}
	}
}
