package org.openmrs.module.operationtheater.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link org.openmrs.module.operationtheater.Surgery} object.
 */
@Handler(supports = { Surgery.class }, order = 50)
public class SurgeryValidator implements Validator {

	/**
	 * Log for this class and subclasses
	 */
	protected final Log log = LogFactory.getLog(getClass());

	private final SchedulingDataValidator schedulingDataValidator = new SchedulingDataValidator();

	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;

	@Autowired
	private OperationTheaterService otService;

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public void setOtService(OperationTheaterService otService) {
		this.otService = otService;
	}

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		}

		return c.equals(Surgery.class);
	}

	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @should throw IllegalArgumentException if obj is null
	 * @should throw IllegalArgumentException if obj is not instance of Surgery
	 * @should fail validation if patient is null
	 * @should fail validation if patient does not exist
	 * @should fail validation if procedure is null
	 * @should fail validation if date started is before date created
	 * @should fail validation if date finished is before date started
	 * @should fail validation if date finished is set and date started is null
	 * @should fail validation if schedulingData validation fails
	 * @should pass validation if all required fields have proper values
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}

		if (obj == null || !(obj instanceof Surgery)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type " + Surgery.class);
		}
		Surgery surgery = (Surgery) obj;

		validatePatientField(surgery, errors);
		validateProcedureField(surgery, errors);
		validateDateStartedField(surgery, errors);
		validateDateFinishedField(surgery, errors);
		validateSchedulingData(surgery, errors);

		//TODO check if each provider in surgicalTeam exists in the db?
	}

	private void validatePatientField(Surgery surgery, Errors errors) {
		if (surgery.getPatient() == null) {
			errors.rejectValue("patient", "operationtheater.surgery.validationError.nullPatient");
			return;
		}
		if (patientService.getPatient(surgery.getPatient().getId()) == null) {
			errors.rejectValue("patient", "operationtheater.surgery.validationError.patientDoesNotExist");
		}
	}

	private void validateProcedureField(Surgery surgery, Errors errors) {
		if (surgery.getProcedure() == null) {
			errors.rejectValue("procedure", "operationtheater.surgery.validationError.nullProcedure");
		}
	}

	private void validateDateStartedField(Surgery surgery, Errors errors) {
		DateTime dateCreated = new DateTime(surgery.getDateCreated());
		DateTime dateStarted = surgery.getDateStarted();

		if (dateStarted == null) {
			return;
		}

		if (dateCreated == null || dateStarted.isBefore(dateCreated)) {
			errors.rejectValue("dateStarted", "operationtheater.surgery.validationError.dateStartedInvalid");
		}

		//hibernate caches objects and returns; -> surgeryInDb == surgery and this check doesn't work
		//moved check to FragmentAction
		//		Surgery surgeryInDb = otService.getSurgery(surgery.getSurgeryId());
		//		if (surgeryInDb.getDateStarted() != null && !surgeryInDb.getDateStarted().equals(surgery.getDateStarted())) {
		//			errors.rejectValue("dateStarted", "operationtheater.surgery.validationError.dateStartedCantBeOverridden");
		//		}
	}

	private void validateDateFinishedField(Surgery surgery, Errors errors) {
		DateTime dateCreated = new DateTime(surgery.getDateCreated());
		DateTime dateStarted = surgery.getDateStarted();
		DateTime dateFinished = surgery.getDateFinished();

		if (dateFinished == null) {
			return;
		}

		if (dateStarted == null || dateFinished.isBefore(dateStarted)) {
			errors.rejectValue("dateFinished", "operationtheater.surgery.validationError.dateFinishedInvalid");
		}

		//hibernate caches objects and returns; -> surgeryInDb == surgery and this check doesn't work
		//moved check to FragmentAction
		//		Surgery surgeryInDb = otService.getSurgery(surgery.getSurgeryId());
		//		if (surgeryInDb.getDateFinished() != null && !surgeryInDb.getDateFinished().equals(surgery.getDateFinished())) {
		//			errors.rejectValue("dateFinished", "operationtheater.surgery.validationError.dateFinishedCantBeOverridden");
		//		}
	}

	private void validateSchedulingData(Surgery surgery, Errors errors) {
		SchedulingData schedulingData = surgery.getSchedulingData();
		if (schedulingData != null) {
			Errors schedulingDataErrors = new BindException(surgery.getSchedulingData(), "schedulingData");
			schedulingDataValidator.validate(schedulingData, schedulingDataErrors);
			if (schedulingDataErrors.hasErrors()) {
				errors.rejectValue("schedulingData", "operationtheater.surgery.validationError.schedulingDataInvalid");
			}
		}
	}
}
