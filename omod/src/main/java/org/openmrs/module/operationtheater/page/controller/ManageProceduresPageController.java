package org.openmrs.module.operationtheater.page.controller;

import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class ManageProceduresPageController {

	public void get(PageModel model,
	                UiUtils ui,
	                @RequestParam(value = "deleted", required = false) String procedureDeleted,
	                @SpringBean OperationTheaterService service) throws Exception {

		String resultMessage = "";
		if (procedureDeleted.equals("true")) {
			resultMessage = ui.message("operationtheater.manageprocedures.success");
		}
		model.addAttribute("resultMessage", resultMessage);

		List<Procedure> procedureList = service.getAllProcedures(false);
		model.addAttribute("procedureList", procedureList);
	}
}
