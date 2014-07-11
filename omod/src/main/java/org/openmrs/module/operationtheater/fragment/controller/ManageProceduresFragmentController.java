package org.openmrs.module.operationtheater.fragment.controller;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

public class ManageProceduresFragmentController {

	/**
	 * @param ui
	 * @param procedureId
	 * @param service
	 * @return
	 * @should retire procedure and return success result
	 * @should return failure result if no procedure with given primary key is found in the db
	 * @should return failure result if user is not allowed to retire a procedure
	 */
	public FragmentActionResult retireProcedure(UiUtils ui,
	                                            @RequestParam(value = "procedureId", required = true) int procedureId,
	                                            @SpringBean OperationTheaterService service) {

		Procedure procedureToRetire = service.getProcedure(procedureId);

		if (procedureToRetire != null) {
			try {
				service.retireProcedure(procedureToRetire,
						"Retired procedure by system administration");
				return new SuccessResult("operationtheater.procedure.retiredSuccessfully");
			}
			catch (APIAuthenticationException e) {
				return new FailureResult(ui.message("operationtheater.procedure.retire.notAllowed"));
			}
		} else {
			return new FailureResult(ui.message("operationtheater.procedure.notFound"));
		}
	}
}
