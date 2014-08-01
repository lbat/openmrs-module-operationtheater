package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;

/**
 * Created by lukas on 29.07.14.
 */
public class Anchor implements TimetableEntry {

	private Location location;

	private DateTime start;
	//	private DateTime

	public Anchor(Location location, DateTime start) {
		this.location = location;
		this.start = start;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public DateTime getStart() {
		return start;
	}

	@Override
	public DateTime getEnd() {
		return start;
	}

	@Override
	public String toString() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM HH:mm");
		String startStr = start == null ? "null      " : fmt.print(start);
		return "Anchor{" +
				"location=" + location.getName() +
				", start=" + startStr +
				'}';
	}
}
