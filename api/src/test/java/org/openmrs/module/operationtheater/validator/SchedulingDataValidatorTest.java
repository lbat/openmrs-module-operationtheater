package org.openmrs.module.operationtheater.validator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.SchedulingData;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchedulingDataValidator}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class SchedulingDataValidatorTest {

	private final String MESSAGE_PREFIX = "operationtheater.schedulingData.validation.";

	private SchedulingDataValidator validator;

	private Errors errors;

	@Before
	public void setUp() throws Exception {
		validator = new SchedulingDataValidator();
	}

	private void mockServiceLayer(SchedulingData data) {
		PowerMockito.mockStatic(Context.class);
		LocationService locationService = Mockito.mock(LocationService.class);
		when(locationService.getLocation(1)).thenReturn(data.getLocation());
		PowerMockito.when(Context.getLocationService()).thenReturn(locationService);
	}

	private SchedulingData createValidSchedulingData() {
		SchedulingData data = new SchedulingData();
		data.setDateLocked(false);
		data.setStart(new DateTime());
		data.setEnd(new DateTime().plusHours(1));

		Location location = new Location();
		location.setId(1);
		location.setUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		HashSet<LocationTag> tags = new HashSet<LocationTag>();
		LocationTag tag = new LocationTag();
		tag.setUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		tags.add(tag);
		location.setTags(tags);
		data.setLocation(location);
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
		SchedulingData data = createValidSchedulingData();
		data.setDateLocked(null);

		//mock service layer
		mockServiceLayer(data);

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
		SchedulingData data = createValidSchedulingData();
		data.setDateLocked(true);
		data.setStart(null);

		//mock service layer
		mockServiceLayer(data);

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
		SchedulingData data = createValidSchedulingData();
		data.setLocation(null);

		//mock service layer
		mockServiceLayer(data);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("location"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "location.nullErrorMessage"));
	}

	/**
	 * @verifies fail validation if location does not exist
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfLocationDoesNotExist() throws Exception {
		SchedulingData data = createValidSchedulingData();
		Location location = new Location();
		int locationId = 1;
		location.setId(locationId);
		data.setLocation(location);

		//mock service layer
		PowerMockito.mockStatic(Context.class);
		LocationService locationService = Mockito.mock(LocationService.class);
		when(locationService.getLocation(locationId)).thenReturn(null);
		PowerMockito.when(Context.getLocationService()).thenReturn(locationService);

		//call method under test
		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("location"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "location.doesNotExist"));
	}

	/**
	 * @verifies fail if location is not tagged as an operation theater
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfLocationIsNotTaggedAsAnOperationTheater() throws Exception {
		SchedulingData data = createValidSchedulingData();
		data.getLocation().setTags(new HashSet<LocationTag>());

		//mock service layer
		mockServiceLayer(data);

		//call method under test
		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);

		//verify
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("location"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "location.missingOTTag"));
	}

	/**
	 * @verifies pass validation if all fields are valid
	 * @see SchedulingDataValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreValid() throws Exception {
		SchedulingData data = createValidSchedulingData();

		//mock service layer
		mockServiceLayer(data);

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
		SchedulingData data = createValidSchedulingData();
		data.setEnd(null);

		//mock service layer
		mockServiceLayer(data);

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
		SchedulingData data = createValidSchedulingData();
		DateTime end = data.getStart().minusHours(1);
		data.setEnd(end);

		//mock service layer
		mockServiceLayer(data);

		errors = new BindException(data, "schedulingData");
		validator.validate(data, errors);
		assertThat(errors.hasErrors(), is(true));
		assertThat(errors.getFieldErrorCount(), is(1));
		assertThat(errors.getFieldErrors().get(0).getField(), is("end"));
		assertThat(errors.getFieldErrors().get(0).getCode(), is(MESSAGE_PREFIX + "end.isBeforeErrorMessage"));
	}
}
