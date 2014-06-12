package org.openmrs.module.operationtheater.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.operationtheater.Procedure;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests {@link ProcedureValidator}
 */
public class ProcedureValidatorTest {

	private final String MESSAGE_PREFIX = "operationtheater.procedure.";

	private ProcedureValidator validator;

	private Errors errors;

	@Before
	public void setUp() throws Exception {
		validator = new ProcedureValidator();
		errors = mock(Errors.class);
	}

	private Procedure createValidProcedure() {
		Procedure procedure = new Procedure();
		procedure.setProcedureId(1);
		procedure.setName("procedure");
		procedure.setDescription("some description");
		procedure.setInterventionDuration(240);
		procedure.setOtPreparationDuration(30);
		procedure.setInpatientStay(5);
		return procedure;
	}

	/**
	 * @verifies fail validation if obj is not instance of surgery
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObjIsNotInstanceOfSurgery() throws Exception {
		Object obj = new Object();
		validator.validate(obj, errors);
		verify(errors).rejectValue("Procedure", "error.general");
	}

	/**
	 * @verifies fail validation if name is null empty or whitespace or longer than 100 chars
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullEmptyOrWhitespaceOrLongerThan100Chars() throws Exception {
		String fieldName = "name";

		Procedure procedure = createValidProcedure();

		//null
		procedure.setName(null);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is("error.name"));

		// empty
		errors = mock(Errors.class);
		procedure.setName("");
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is("error.name"));

		//whitespace
		errors = mock(Errors.class);
		procedure.setName("   ");
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is("error.name"));

		//longer than 100 chars
		String longName = StringUtils.repeat("*", 101);
		procedure.setName(longName);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "longName.errorMessage"));
	}

	/**
	 * @verifies fail validation if description is longer than 1024 chars
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDescriptionIsLongerThan1024Chars() throws Exception {
		String fieldName = "description";
		String messageId = MESSAGE_PREFIX + fieldName + ".errorMessage";
		Procedure procedure = createValidProcedure();

		//longer than 1024 chars
		errors = new BindException(procedure, "procedure");
		String longDescription = StringUtils.repeat("*", 1025);
		procedure.setDescription(longDescription);

		validator.validate(procedure, errors);

		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));
	}

	/**
	 * @verifies fail validation if interventionDuration is null negative or greater than 24 hours
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfInterventionDurationIsNullNegativeOrGreaterThan24Hours() throws Exception {
		String fieldName = "interventionDuration";
		String messageId = MESSAGE_PREFIX + fieldName + ".errorMessage";
		Procedure procedure = createValidProcedure();

		//null
		procedure.setInterventionDuration(null);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		// negative
		procedure.setInterventionDuration(-1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		//longer than 24 hours
		procedure.setInterventionDuration(24 * 60 + 1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

	}

	/**
	 * @verifies fail validation if otPreparationDuration is null negative or greater than 5 hours
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOtPreparationDurationIsNullNegativeOrGreaterThan5Hours() throws Exception {
		String fieldName = "otPreparationDuration";
		String messageId = MESSAGE_PREFIX + fieldName + ".errorMessage";
		Procedure procedure = createValidProcedure();

		//null
		procedure.setOtPreparationDuration(null);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		// negative
		procedure.setOtPreparationDuration(-1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		//longer than 5 hours
		procedure.setOtPreparationDuration(5 * 60 + 1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

	}

	/**
	 * @verifies fail validation if inpatientStay is null negative or greater than 60 days
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfInpatientStayIsNullNegativeOrGreaterThan60Days() throws Exception {
		String fieldName = "otPreparationDuration";
		String messageId = MESSAGE_PREFIX + fieldName + ".errorMessage";
		Procedure procedure = createValidProcedure();

		//null
		procedure.setOtPreparationDuration(null);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		// negative
		procedure.setOtPreparationDuration(-1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));

		//longer than 5 hours
		procedure.setOtPreparationDuration(5 * 60 + 1);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is(fieldName));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(messageId));
	}

	/**
	 * @verifies pass validation if all fields are valid
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreValid() throws Exception {
		Procedure procedure = createValidProcedure();
		procedure.setDescription(null);
		errors = new BindException(procedure, "procedure");
		validator.validate(procedure, errors);

		assertThat(errors.hasErrors(), is(false));
	}
}
