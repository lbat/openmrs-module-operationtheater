package org.openmrs.module.operationtheater.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.operationtheater.Procedure;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests {@link ProcedureValidator}
 */
public class ProcedureValidatorTest {

	private final String MESSAGE_PREFIX = "operationtheater.Procedure.";

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
		Procedure procedure = createValidProcedure();

		//null
		procedure.setName(null);
		validator.validate(procedure, errors);
		verify(errors).rejectValue("name", MESSAGE_PREFIX + "name.errorMessage", null, null);

		// empty
		errors = mock(Errors.class);
		procedure.setName("");
		validator.validate(procedure, errors);
		verify(errors).rejectValue("name", MESSAGE_PREFIX + "name.errorMessage", null, null);

		//whitespace
		errors = mock(Errors.class);
		procedure.setName("   ");
		validator.validate(procedure, errors);
		verify(errors).rejectValue("name", MESSAGE_PREFIX + "name.errorMessage", null, null);

		//longer than 100 chars
		errors = mock(Errors.class);
		String longName = StringUtils.repeat("*", 101);
		procedure.setName(longName);
		validator.validate(procedure, errors);
		verify(errors).rejectValue("name", MESSAGE_PREFIX + "name.errorMessage", null, null);
	}

	/**
	 * @verifies fail validation if description is null empty or whitespace or longer than 1024 chars
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDescriptionIsNullEmptyOrWhitespaceOrLongerThan1024Chars() throws Exception {
		String fieldName = "description";
		String messageId = MESSAGE_PREFIX + fieldName + ".errorMessage";
		Procedure procedure = createValidProcedure();

		//null
		procedure.setDescription(null);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		// empty
		errors = mock(Errors.class);
		procedure.setDescription("");
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		//whitespace
		errors = mock(Errors.class);
		procedure.setDescription("   ");
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		//longer than 1024 chars
		errors = mock(Errors.class);
		String longDescription = StringUtils.repeat("*", 1025);
		procedure.setDescription(longDescription);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);
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
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		// negative
		errors = mock(Errors.class);
		procedure.setInterventionDuration(-1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		//longer than 24 hours
		errors = mock(Errors.class);
		procedure.setInterventionDuration(24 * 60 + 1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);
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
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		// negative
		errors = mock(Errors.class);
		procedure.setOtPreparationDuration(-1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		//longer than 5 hours
		errors = mock(Errors.class);
		procedure.setOtPreparationDuration(5 * 60 + 1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);
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
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		// negative
		errors = mock(Errors.class);
		procedure.setOtPreparationDuration(-1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);

		//longer than 5 hours
		errors = mock(Errors.class);
		procedure.setOtPreparationDuration(5 * 60 + 1);
		validator.validate(procedure, errors);
		verify(errors).rejectValue(fieldName, messageId, null, null);
	}

}
