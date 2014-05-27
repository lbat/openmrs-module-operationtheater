package org.openmrs.module.operationtheater.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.operationtheater.Surgery;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests {@link org.openmrs.module.operationtheater.validator.SurgeryValidator}
 */
public class SurgeryValidatorTest {

	private PatientService patientService;

	private SurgeryValidator validator;

	private Errors errors;

	@Before
	public void setUp() throws Exception {
		validator = new SurgeryValidator();
		errors = mock(Errors.class);
		patientService = mock(PatientService.class);
		validator.setPatientService(patientService);
	}

	/**
	 * @verifies fail validation if patient is null or empty
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientIsNullOrEmpty() throws Exception {
		Surgery surgery = new Surgery();
		validator.validate(surgery, errors);
		Mockito.verify(errors).rejectValue("patient", "operationtheater.Surgery.emptyPatientID");
	}

	/**
	 * @verifies pass validation if all required fields have proper values
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		Surgery surgery = new Surgery();
		Patient patient = new Patient();
		int id = 1;
		patient.setId(id);
		when(patientService.getPatient(id)).thenReturn(patient);

		surgery.setPatient(patient);
		validator.validate(surgery, errors);
		Mockito.verify(errors, never()).rejectValue(Matchers.anyString(), Matchers.anyString());
	}

	/**
	 * @verifies fail validation if obj is not instance of surgery
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObjIsNotInstanceOfSurgery() throws Exception {
		Object o = new Object();
		validator.validate(o, errors);
		Mockito.verify(errors).rejectValue("surgery", "error.general");
	}

	/**
	 * @verifies fail validation if patient does not exist
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientDoesNotExist() throws Exception {
		Patient patient = new Patient();
		int id = 1;
		patient.setId(id);
		when(patientService.getPatient(id)).thenReturn(null);

		Surgery surgery = new Surgery();
		surgery.setPatient(patient);

		validator.validate(surgery, errors);
		Mockito.verify(errors).rejectValue("patient", "operationtheater.surgery.patientDoesNotExist");

	}
}
