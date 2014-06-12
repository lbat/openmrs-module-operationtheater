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

	//if you change this value also change the corresponding error message in messages.properties
	public static final int MAX_INTERVENTION_DURATION = 24 * 60;

	//if you change this value also change the corresponding error message in messages.properties
	public static final int MAX_INPATIENT_STAY = 60;

	//if you change this value also change the corresponding error message in messages.properties
	public static final int MAX_OT_PREPARATION_DURATION = 5 * 60;

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
	 * @should fail validation if description is longer than 1024 chars
	 * @should fail validation if interventionDuration is null negative or greater than 24 hours
	 * @should fail validation if otPreparationDuration is null negative or greater than 5 hours
	 * @should fail validation if inpatientStay is null negative or greater than 60 days
	 * @should pass validation if all fields are valid
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
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
		if (verifyIfNameHasMoreThan100Characters(procedure.getName())) {//TODO adjust to not longer than 255
			errors.rejectValue("name", "operationtheater.procedure.longName.errorMessage");
		}
		//Todo check for duplicate names as in AppointmentTypeValidator ?!
	}

	private boolean verifyIfNameHasMoreThan100Characters(String appointmentName) {
		if (appointmentName != null) {
			return (appointmentName.length() > 100) ? true : false;
		}
		return false;
	}

	private void validateDescriptionField(Errors errors, String description) {
		if (verifyIfDescriptionHasMoreThan1024Characters(description)) {
			errors.rejectValue("description", "operationtheater.procedure.description.errorMessage");
		}
	}

	private boolean verifyIfDescriptionHasMoreThan1024Characters(String description) {
		if (description != null) {
			return (description.length() > 1024) ? true : false;
		}
		return false;
	}

	private void validateInterventionDurationField(Errors errors, Procedure procedure) {
		Integer interventionDuration = procedure.getInterventionDuration();
		if (interventionDuration == null || interventionDuration <= 0 || interventionDuration > MAX_INTERVENTION_DURATION) {
			errors.rejectValue("interventionDuration", "operationtheater.procedure.interventionDuration.errorMessage");
		}
	}

	private void validateOtPreparationDurationField(Errors errors, Procedure procedure) {
		Integer otPreparationDuration = procedure.getOtPreparationDuration();
		if (otPreparationDuration == null || otPreparationDuration <= 0
				|| otPreparationDuration > MAX_OT_PREPARATION_DURATION) {
			errors.rejectValue("otPreparationDuration", "operationtheater.procedure.otPreparationDuration.errorMessage");
		}
	}

	private void validateInpatientStayField(Errors errors, Procedure procedure) {
		Integer inpatientStay = procedure.getInpatientStay();
		if (inpatientStay == null || inpatientStay <= 0 || inpatientStay > MAX_INPATIENT_STAY) {
			errors.rejectValue("inpatientStay", "operationtheater.procedure.inpatientStay.errorMessage");
		}
	}
	//TODO add error message strings to messages.properties
}
