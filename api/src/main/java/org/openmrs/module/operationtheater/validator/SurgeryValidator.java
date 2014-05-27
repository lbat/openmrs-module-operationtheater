package org.openmrs.module.operationtheater.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.module.operationtheater.Surgery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link org.openmrs.module.operationtheater.Surgery} object.
 */
@Handler(supports = { Surgery.class }, order = 50)
public class SurgeryValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(Surgery.class);
	}

	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if obj is not instance of surgery
	 * @should fail validation if patient is null or empty
	 * @should fail validation if patient does not exist
	 * @should pass validation if all required fields have proper values
	 */
	public void validate(Object obj, Errors errors) {
		if(!(obj instanceof  Surgery)){
			errors.rejectValue("surgery", "error.general");
			return;
		}
		Surgery surgery = (Surgery) obj;
		if (surgery == null) {
			errors.rejectValue("surgery", "error.general");
			return;
		}
		if(surgery.getPatient() == null){
			errors.rejectValue("patient", "operationtheater.Surgery.emptyPatientID");
			return;
		}
		if(patientService.getPatient(surgery.getPatient().getId()) == null) {
			errors.rejectValue("patient", "operationtheater.surgery.patientDoesNotExist");
		}
	}
}
