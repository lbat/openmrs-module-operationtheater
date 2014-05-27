package org.openmrs.module.operationtheater.rest.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping("/rest/" + RestConstants.VERSION_1 + OperationTheaterRestController.OPERATION_THEATER_REST_NAMESPACE)
@RequestMapping("/rest/v1/operationtheater")
public class OperationTheaterRestController extends MainResourceController {

	public static final String OPERATION_THEATER_REST_NAMESPACE = "/operationtheater";

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
	 * @should return v1 slash operationtheater
	 */
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + OperationTheaterRestController.OPERATION_THEATER_REST_NAMESPACE;
	}
}
