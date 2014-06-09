package org.openmrs.module.operationtheater.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.operationtheater.Procedure;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link org.openmrs.module.operationtheater.Surgery} object.
 */
@Handler(supports = { Procedure.class }, order = 50)
public class ProcedureValidator implements Validator {

	private static final int MAX_INTERVENTION_DURATION = 24 * 60;

	private static final int MAX_INPATIENT_STAY = 60;

	private static final int MAX_OT_PREPARATION_DURATION = 5 * 60;

	/**
	 * Log for this class and subclasses
	 */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(Procedure.class);
	}

	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @should fail validation if obj is not instance of surgery
	 * @should fail validation if name is null empty or whitespace or longer than 100 chars
	 * @should fail validation if description is null empty or whitespace or longer than 1024 chars
	 * @should fail validation if interventionDuration is null negative or greater than 24 hours
	 * @should fail validation if otPreparationDuration is null negative or greater than 5 hours
	 * @should fail validation if inpatientStay is null negative or greater than 60 days
	 * @see org.springframework.validation.Validator#validate(Object,
	 * org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		if (!(obj instanceof Procedure)) {
			errors.rejectValue("Procedure", "error.general");
			return;
		}
		Procedure procedure = (Procedure) obj;
		if (procedure == null) {
			errors.rejectValue("Procedure", "error.general");
		} else {
			validateNameField(errors, procedure);
			validateDescriptionField(errors, procedure.getDescription());
			validateInterventionDurationField(errors, procedure);
			validateOtPreparationDurationField(errors, procedure);
			validateInpatientStayField(errors, procedure);
		}
	}

	private void validateNameField(Errors errors, Procedure procedure) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "operationtheater.Procedure.name.errorMessage");
		if (verifyIfNameHasMoreThan100Characters(procedure.getName())) {
			errors.rejectValue("name", "operationtheater.Procedure.name.errorMessage");
		}
	}

	private boolean verifyIfNameHasMoreThan100Characters(String appointmentName) {
		if (appointmentName != null) {
			return (appointmentName.length() > 100) ? true : false;
		}
		return false;
	}

	private void validateDescriptionField(Errors errors, String description) {
		ValidationUtils
				.rejectIfEmptyOrWhitespace(errors, "description", "operationtheater.Procedure.description.errorMessage");
		if (verifyIfDescriptionHasMoreThan1024Characters(description)) {
			errors.rejectValue("description", "operationtheater.Procedure.description.errorMessage");
		}
	}

	private boolean verifyIfDescriptionHasMoreThan1024Characters(String description) {
		if (description != null) {
			return (description.length() > 1024) ? true : false;
		}
		return false;
	}

	private void validateInterventionDurationField(Errors errors, Procedure procedure) {
		ValidationUtils.rejectIfEmpty(errors, "interventionDuration",
				"operationtheater.Procedure.interventionDuration.errorMessage");
		Integer interventionDuration = procedure.getInterventionDuration();
		if (interventionDuration == null || interventionDuration <= 0 || interventionDuration > MAX_INTERVENTION_DURATION) {
			errors.rejectValue("interventionDuration", "operationtheater.Procedure.interventionDuration.errorMessage");
		}
	}

	private void validateOtPreparationDurationField(Errors errors, Procedure procedure) {
		ValidationUtils.rejectIfEmpty(errors, "otPreparationDuration",
				"operationtheater.Procedure.otPreparationDuration.errorMessage");
		Integer otPreparationDuration = procedure.getOtPreparationDuration();
		if (otPreparationDuration == null || otPreparationDuration <= 0
				|| otPreparationDuration > MAX_OT_PREPARATION_DURATION) {
			errors.rejectValue("otPreparationDuration", "operationtheater.Procedure.otPreparationDuration.errorMessage");
		}
	}

	private void validateInpatientStayField(Errors errors, Procedure procedure) {
		ValidationUtils.rejectIfEmpty(errors, "inpatientStay", "operationtheater.Procedure.inpatientStay.errorMessage");
		Integer inpatientStay = procedure.getInpatientStay();
		if (inpatientStay == null || inpatientStay <= 0 || inpatientStay > MAX_INPATIENT_STAY) {
			errors.rejectValue("inpatientStay", "operationtheater.Procedure.inpatientStay.errorMessage");
		}
	}
	//TODO add error message strings to messages.properties
}
