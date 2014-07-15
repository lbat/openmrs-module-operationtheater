package org.openmrs.module.operationtheater.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.SchedulingData;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link SchedulingData} object.
 */
@Handler(supports = { SchedulingData.class }, order = 50)
public class SchedulingDataValidator implements Validator {

	/**
	 * Log for this class and subclasses
	 */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see Validator#supports(Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		}
		return c.equals(SchedulingData.class);
	}

	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @should throw IllegalArgumentException if obj is null
	 * @should throw IllegalArgumentException if obj is not instance of Procedure
	 * @should fail validation if dateLocked is null
	 * @should fail validation if dateLocked is true and scheduledDateTime is null
	 * @should fail validation if start is not null and scheduled location is null
	 * @should fail validation if location does not exist
	 * @should fail if location is not tagged as an operation theater
	 * @should fail validation if start is not null and end is null
	 * @should fail validation if start is after end
	 * @should pass validation if all fields are valid
	 * @see Validator#validate(Object, Errors)
	 */
	public void validate(Object obj, Errors errors) {
		if (log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".validate...");
		}

		if (obj == null || !(obj instanceof SchedulingData)) {
			throw new IllegalArgumentException(
					"The parameter obj should not be null and must be of type " + SchedulingData.class);
		}

		SchedulingData schedulingData = (SchedulingData) obj;

		validateDateLocked(errors, schedulingData);
		validateStart(errors, schedulingData);
		validateEnd(errors, schedulingData);
		validateLocation(errors, schedulingData);
	}

	private void validateDateLocked(Errors errors, SchedulingData schedulingData) {
		if (schedulingData.getDateLocked() == null) {
			errors.rejectValue("dateLocked", "operationtheater.schedulingData.validation.dateLocked.errorMessage");
		}
	}

	private void validateStart(Errors errors, SchedulingData sd) {
		if (sd.getDateLocked() != null && sd.getDateLocked() == true && sd.getStart() == null) {
			errors.rejectValue("start", "operationtheater.schedulingData.validation.start.errorMessage");
		}
	}

	private void validateEnd(Errors errors, SchedulingData sd) {
		if (sd.getStart() != null && sd.getEnd() == null) {
			errors.rejectValue("end", "operationtheater.schedulingData.validation.end.nullErrorMessage");
		} else if (sd.getStart() != null && sd.getEnd() != null && sd.getStart().isAfter(sd.getEnd())) {
			errors.rejectValue("end", "operationtheater.schedulingData.validation.end.isBeforeErrorMessage");
		}
	}

	private void validateLocation(Errors errors, SchedulingData data) {
		Location location = data.getLocation();
		if (location == null && data.getStart() != null) {
			errors.rejectValue("location", "operationtheater.schedulingData.validation.location.nullErrorMessage");
			return;
		} else if (location == null && data.getStart() == null) {
			return;
		}

		LocationService locationService = Context.getLocationService();
		Location locationFromDb = locationService.getLocation(location.getId());
		if (locationFromDb == null) {
			errors.rejectValue("location", "operationtheater.schedulingData.validation.location.doesNotExist");
			return;
		}
		boolean otTagPresent = false;
		for (LocationTag tag : locationFromDb.getTags()) {
			if (tag.getUuid().equals(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID)) {
				otTagPresent = true;
			}
		}
		if (!otTagPresent) {
			errors.rejectValue("location", "operationtheater.schedulingData.validation.location.missingOTTag");
		}
	}
}
