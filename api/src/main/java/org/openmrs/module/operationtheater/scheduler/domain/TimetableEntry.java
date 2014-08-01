package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.openmrs.Location;

/**
 * Common interface for a timetable entry<br />
 * Will be implemented by {@link Anchor} and {@link PlannedSurgery}
 */
public interface TimetableEntry {

	Location getLocation();

	DateTime getStart();

	DateTime getEnd();
}
