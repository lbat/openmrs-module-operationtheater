package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;

/**
 * Anchor point for a planning chain that is needed by optaplanner library
 */
public class Anchor implements TimetableEntry {

	private Location location;

	private DateTime start;

	private TimetableEntry nextTimeTableEntry;

	/**
	 * number of minutes until the start of next chain or end of available time
	 */
	private int maxChainLengthInMinutes;

	public Anchor(Location location, DateTime start) {
		this.location = location;
		this.start = start;
	}

	public void setMaxChainLengthInMinutes(int maxChainLengthInMinutes) {
		this.maxChainLengthInMinutes = maxChainLengthInMinutes;
	}

	/**
	 * @return
	 * @should return maxChainLengthInMinutes if no timetable element is attached
	 * @should return maxChainLengthInMinutes minus length of successor chain
	 */
	public int getRemainingTime() {
		return maxChainLengthInMinutes - getChainLengthInMinutes();
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
	public int getChainLengthInMinutes() {
		if (nextTimeTableEntry != null) {
			return nextTimeTableEntry.getChainLengthInMinutes();
		}
		return 0;
	}

	@Override
	public TimetableEntry getNextTimetableEntry() {
		return nextTimeTableEntry;
	}

	@Override
	public void setNextTimetableEntry(TimetableEntry nextTimeTableEntry) {
		this.nextTimeTableEntry = nextTimeTableEntry;
	}

	@Override
	public String toString() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM HH:mm");
		String startStr = start == null ? "null      " : fmt.print(start);
		String locationName = location != null ? location.getName() : "null";
		return "Anchor{" +
				" " + locationName +
				", " + startStr +
				'}';
	}
}
