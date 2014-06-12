package org.openmrs.module.operationtheater.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.validator.ProcedureValidator;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public class CreateEditProcedurePageController {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @param model
	 * @param procedureId
	 * @param operationTheaterService
	 * @should get procedure from db if parameter id is not null
	 * @should add empty procedure to the model
	 */
	public void get(PageModel model,
	                @RequestParam(value = "procedureId", required = false) Integer procedureId,
	                @SpringBean OperationTheaterService operationTheaterService) {

		Procedure procedure = new Procedure();
		if (procedureId != null) {
			procedure = operationTheaterService.getProcedure(procedureId);
		}
		model.addAttribute("procedure", procedure);

		model.addAttribute("maxInterventionDuration", ProcedureValidator.MAX_INTERVENTION_DURATION);
		model.addAttribute("maxOtPreparationDuration", ProcedureValidator.MAX_OT_PREPARATION_DURATION);
		model.addAttribute("maxInpatientStay", ProcedureValidator.MAX_INPATIENT_STAY);
	}

	public String post(PageModel model,
	                   @ModelAttribute("procedure") @BindParams Procedure procedure,
	                   BindingResult errors,
	                   @SpringBean OperationTheaterService service,
	                   @SpringBean ProcedureValidator procedureValidator) {

		System.err.println(procedure.getInpatientStay());
		System.err.println(procedure.getInterventionDuration());
		System.err.println(procedure.getOtPreparationDuration());
		System.err.println(procedure.getDescription());
		System.err.println(procedure.getName());
		if (!errors.hasErrors()) {
			System.err.println("HAS NO ERRORS");
		}

		Errors newErrors = new BindException(procedure, "procedure");
		procedureValidator.validate(procedure, newErrors);
		if (!newErrors.hasErrors()) {
			System.err.println("HAS NO ERRORS");
			try {
				service.saveProcedure(procedure);
				System.err.println("saved successfully: redirecting");
				return "redirect:/operationtheater/manageProcedures.page";
			}
			catch (Exception e) {
				log.warn("Some error occurred while saving procedures details:", e);
			}
		}

		System.err.println("NOT saved successfully");
		for (ObjectError error : newErrors.getAllErrors()) {
			System.err.println(error.toString());
			for (String e : error.getCodes()) {
				System.err.println("    " + e);
			}
		}

		model.addAttribute("errors", newErrors);
		model.addAttribute("procedure", procedure);

		model.addAttribute("maxInterventionDuration", ProcedureValidator.MAX_INTERVENTION_DURATION);
		model.addAttribute("maxOtPreparationDuration", ProcedureValidator.MAX_OT_PREPARATION_DURATION);
		model.addAttribute("maxInpatientStay", ProcedureValidator.MAX_INPATIENT_STAY);

		return "createEditProcedure";

	}
}
