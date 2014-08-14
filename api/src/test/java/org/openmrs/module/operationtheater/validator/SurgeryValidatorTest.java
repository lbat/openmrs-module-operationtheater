package org.openmrs.module.operationtheater.validator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.operationtheater.MockUtil;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests {@link SurgeryValidator}
 */
public class SurgeryValidatorTest {

	private PatientService patientService;

	private OperationTheaterService otService;

	private SurgeryValidator validator;

	//	private Errors errors;

	@Before
	public void setUp() throws Exception {
		validator = new SurgeryValidator();
		//		errors = mock(Errors.class);
		patientService = mock(PatientService.class);
		validator.setPatientService(patientService);
		otService = mock(OperationTheaterService.class);
		validator.setOtService(otService);
	}

	private Surgery createValidSurgery() {
		Surgery surgery = new Surgery();
		surgery.setId(1);
		Patient patient = new Patient();
		patient.setId(1);
		surgery.setPatient(patient);
		surgery.setProcedure(new Procedure());
		surgery.setSchedulingData(new SchedulingData());
		DateTime refDate = new DateTime();
		surgery.setDateCreated(refDate.toDate());
		//		surgery.setDateStarted(refDate.plusHours(1));
		//		surgery.setDateFinished(refDate.plusHours(2));
		return surgery;
	}

	/**
	 * @verifies throw IllegalArgumentException if obj is null
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldThrowIllegalArgumentExceptionIfObjIsNull() throws Exception {
		Errors errors = Mockito.mock(Errors.class);
		validator.validate(null, errors);
	}

	/**
	 * @verifies throw IllegalArgumentException if obj is not instance of Surgery
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldThrowIllegalArgumentExceptionIfObjIsNotInstanceOfSurgery() throws Exception {
		Errors errors = Mockito.mock(Errors.class);
		validator.validate(new Object(), errors);
	}

	/**
	 * @verifies fail validation if patient is null
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientIsNull() throws Exception {
		Surgery surgery = createValidSurgery();
		surgery.setPatient(null);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("patient").getCode(),
				is("operationtheater.surgery.validationError.nullPatient"));
		//		Mockito.verify(errors).rejectValue("patient", "operationtheater.surgery.validationError.nullPatient");
	}

	/**
	 * @verifies pass validation if all required fields have proper values
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		Surgery surgery = createValidSurgery();
		Patient patient = new Patient();
		int id = 1;
		patient.setId(id);
		when(patientService.getPatient(id)).thenReturn(patient);

		surgery.setPatient(patient);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(false));
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

		Surgery surgery = createValidSurgery();
		surgery.setPatient(patient);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("patient").getCode(),
				is("operationtheater.surgery.validationError.patientDoesNotExist"));
		//		Mockito.verify(errors).rejectValue("patient", "operationtheater.surgery.validationError.patientDoesNotExist");

	}

	/**
	 * @verifies fail validation if procedure is null
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfProcedureIsNull() throws Exception {
		Surgery surgery = createValidSurgery();
		surgery.setProcedure(null);
		when(patientService.getPatient(1)).thenReturn(surgery.getPatient());

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("procedure").getCode(),
				is("operationtheater.surgery.validationError.nullProcedure"));
	}

	/**
	 * @verifies fail validation if date started is before date created
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateStartedIsBeforeDateCreated() throws Exception {
		Surgery surgery = createValidSurgery();
		surgery.setDateStarted(new DateTime(surgery.getDateCreated()).minusHours(1));
		when(patientService.getPatient(1)).thenReturn(surgery.getPatient());
		Surgery surgeryInDb = new Surgery();
		when(otService.getSurgery(1)).thenReturn(surgeryInDb);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("dateStarted").getCode(),
				is("operationtheater.surgery.validationError.dateStartedInvalid"));
	}

	/**
	 * @verifies fail validation if date finished is before date started
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateFinishedIsBeforeDateStarted() throws Exception {
		Surgery surgery = createValidSurgery();
		DateTime dateStarted = new DateTime();
		surgery.setDateStarted(dateStarted);
		surgery.setDateFinished(new DateTime().minusHours(1));
		when(patientService.getPatient(1)).thenReturn(surgery.getPatient());
		Surgery surgeryInDb = new Surgery();
		when(otService.getSurgery(1)).thenReturn(surgeryInDb);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("dateFinished").getCode(),
				is("operationtheater.surgery.validationError.dateFinishedInvalid"));
	}

	/**
	 * @verifies fail validation if date finished is set and date started is null
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateFinishedIsSetAndDateStartedIsNull() throws Exception {
		Surgery surgery = createValidSurgery();
		surgery.setDateStarted(null);
		surgery.setDateFinished(new DateTime());
		when(patientService.getPatient(1)).thenReturn(surgery.getPatient());
		Surgery surgeryInDb = new Surgery();
		when(otService.getSurgery(1)).thenReturn(surgeryInDb);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("dateFinished").getCode(),
				is("operationtheater.surgery.validationError.dateFinishedInvalid"));
	}

	/**
	 * @verifies fail validation if schedulingData validation fails
	 * @see SurgeryValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfSchedulingDataValidationFails() throws Exception {
		Surgery surgery = createValidSurgery();
		when(patientService.getPatient(1)).thenReturn(surgery.getPatient());
		SchedulingDataValidator schedulingDataValidator = (SchedulingDataValidator) MockUtil
				.mockValidator(false, SchedulingDataValidator.class, SchedulingData.class, "start", "someError");
		Whitebox.setInternalState(validator, "schedulingDataValidator", schedulingDataValidator);

		Errors errors = new BindException(surgery, "surgery");

		//call method under test
		validator.validate(surgery, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldError("schedulingData").getCode(),
				is("operationtheater.surgery.validationError.schedulingDataInvalid"));
		verify(schedulingDataValidator).validate(Mockito.eq(surgery.getSchedulingData()), Mockito.any(Errors.class));
	}
}
