package org.openmrs.module.operationtheater.validator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.validator.ValidateUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests {@link SchedulingDataValidator}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ValidateUtil.class)
public class SchedulingDataValidatorTest {

	private final String MESSAGE_PREFIX = "operationtheater.schedulingData.validation.";

	private SchedulingDataValidator validator;

	private Errors errors;

	@Before
	public void setUp() throws Exception {
		validator = new SchedulingDataValidator();
	}

	private ArgumentCaptor<Location> mockValidateUtil(final boolean locationValidationShouldPass) throws Exception {
		PowerMockito.spy(ValidateUtil.class);

		//do not execute the validate method
		PowerMockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Errors errors = (Errors) invocationOnMock.getArguments()[1];
				if (!locationValidationShouldPass) {
					errors.rejectValue("location", "code");
				}
				return null;
			}
		}).when(ValidateUtil.class, "validate", Mockito.any(Location.class), Mockito.any(Errors.class));
		return null;
	}

	private SchedulingData createValidSchedulingData() {
		SchedulingData data = new SchedulingData();
		data.setDateLocked(false);
		data.setStart(new DateTime());
		data.setEnd(new DateTime().plusHours(1));
		data.setLocation(new Location());
		return data;
	}

	/**
	 * @verifies throw IllegalArgumentException if obj is null
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldThrowIllegalArgumentExceptionIfObjIsNull() throws Exception {
		validator.validate(null, errors);
	}

	/**
	 * @verifies throw IllegalArgumentException if obj is not instance of Procedure
	 * @see ProcedureValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldThrowIllegalArgumentExceptionIfObjIsNotInstanceOfProcedure() throws Exception {
		validator.validate(new Object(), errors);
	}

	/**
	 * @verifies fail validation if dateLocked is null
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateLockedIsNull() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();
		data.setDateLocked(null);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("dateLocked"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "dateLocked.errorMessage"));
	}

	/**
	 * @verifies fail validation if dateLocked is true and start is null
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateLockedIsTrueAndScheduledDateTimeIsNull() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();
		data.setDateLocked(true);
		data.setStart(null);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("start"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "start.errorMessage"));
	}

	/**
	 * @verifies fail validation if start is not null and scheduled location is null
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfStartIsNotNullAndScheduledLocationIsNull() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();
		data.setLocation(null);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("location"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "location.nullErrorMessage"));
	}

	/**
	 * @verifies fail validation if location is not valid
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfLocationIsNotValid() throws Exception {
		mockValidateUtil(false);
		SchedulingData data = createValidSchedulingData();
		Location location = new Location();
		data.setLocation(location);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("location"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "location.invalidErrorMessage"));
	}

	/**
	 * @verifies pass validation if all fields are valid
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreValid() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(false));

		for (FieldError error : errors.getFieldErrors()) {
			System.err.println(error.getField() + " - " + error.getCode());
		}
	}

	/**
	 * @verifies fail validation if start is not null and end is null
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfStartIsNotNullAndEndIsNull() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();
		data.setEnd(null);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("end"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "end.nullErrorMessage"));
	}

	/**
	 * @verifies fail validation if start is after end
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfStartIsAfterEnd() throws Exception {
		mockValidateUtil(true);
		SchedulingData data = createValidSchedulingData();
		DateTime end = data.getStart().minusHours(1);
		data.setEnd(end);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("end"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "end.isBeforeErrorMessage"));
	}
}
