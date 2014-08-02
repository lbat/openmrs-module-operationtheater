package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.solver.MovablePlannedSurgerySelectionFilter;
import org.openmrs.module.operationtheater.scheduler.solver.PlannedSurgeryDifficultyComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * This class is the Planning entity of this optimization problem
 * This means it contains attributes which will be changed by the solver during the solution finding process
 * This attributes are called Planning Variables - see the @PlanningVariable annotation on the corresponding setters
 * possible values of a planning variable is defined by the value range provider
 */
@PlanningEntity(movableEntitySelectionFilter = MovablePlannedSurgerySelectionFilter.class,
		difficultyComparatorClass = PlannedSurgeryDifficultyComparator.class)
public class PlannedSurgery implements TimetableEntry {

	private OperationTheaterService otService; //= Context.getService(OperationTheaterService.class);

	//
	private Surgery surgery;

	// Planning variables: will change during planning (between score calculations)
	private TimetableEntry previousTimetableEntry;

	//shadow variable - automatically calculated if the underlying genuine planning variable (start) changes its value
	//not changed by the solver
	private DateTime start;

	private DateTime end;

	private Location location;

	private TimetableEntry nextTimetableEntry;

	public PlannedSurgery(OperationTheaterService otService) {
		this.otService = otService;
	}

	public PlannedSurgery() {
	}

	/**
	 * @param other
	 * @return true if intervals overlap
	 * @should return false if any of the date object are null
	 * @should return if the two intervals overlap
	 */
	public boolean isOverlapping(PlannedSurgery other) {
		if (start == null || end == null || other.start == null || other.end == null) {
			return false;
		}
		//if Interval is constructed with a null parameter it assumes a current timestamp
		return new Interval(this.start, this.end).overlaps(new Interval(other.start, other.end));
	}

	/**
	 * convenient method used by drool file and helps to keep this file readable
	 *
	 * @return
	 * @should return true if location start or end variables are null
	 * @should return if current scheduling is outside available times
	 */
	public boolean isOutsideAvailableTimes() {
		if (start == null || end == null || location == null) {
			return true;
		}

		Interval available = otService.getLocationAvailableTime(location, start);
		Interval scheduled = new Interval(start, end);
		Interval overlap = scheduled.overlap(available);
		return !scheduled.equals(overlap);
	}

	public Surgery getSurgery() {
		return surgery;
	}

	public void setSurgery(Surgery surgery) {
		this.surgery = surgery;
	}

	@PlanningVariable(chained = true, valueRangeProviderRefs = { "anchorRange", "plannedSurgeryRange" })
	public TimetableEntry getPreviousTimetableEntry() {
		return previousTimetableEntry;
	}

	public void setPreviousTimetableEntry(TimetableEntry previousTimetableEntry) {
		this.previousTimetableEntry = previousTimetableEntry;
		setStart(previousTimetableEntry.getEnd());
		location = previousTimetableEntry.getLocation();
	}

	@Override
	public TimetableEntry getNextTimetableEntry() {
		return nextTimetableEntry;
	}

	@Override
	public void setNextTimetableEntry(TimetableEntry nextTimeTableEntry) {
		this.nextTimetableEntry = nextTimeTableEntry;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public DateTime getStart() {
		return start;
	}

	/**
	 * @param start
	 * @should set start and update end accordingly
	 */
	public void setStart(DateTime start) {
		setStart(start, true);
	}

	/**
	 * convenient method used for junit testing
	 *
	 * @param start
	 * @param calculateEndTime
	 */
	public void setStart(DateTime start, boolean calculateEndTime) {
		this.start = start;
		if (calculateEndTime) {
			if (start == null) {
				end = null;
			} else {
				int interventionDuration = surgery.getProcedure().getInterventionDuration();
				int otPreparationDuration = surgery.getProcedure().getOtPreparationDuration();
				DateTime endDate = start.plusMinutes(interventionDuration + otPreparationDuration);
				setEnd(endDate);
			}
		}
	}

	public DateTime getEnd() {
		return end;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}

	/**
	 * @return
	 * @should return the entire duration this surgery occupies the ot when nextTimeTableEntry is null
	 * @should return value from its successor in the chain added to the entire duration this surgery occupies the ot
	 */
	@Override
	public int getChainLengthInMinutes() {
		Procedure procedure = surgery.getProcedure();
		int duration = procedure.getInterventionDuration() + procedure.getOtPreparationDuration();
		return nextTimetableEntry == null ? duration : nextTimetableEntry.getChainLengthInMinutes() + duration;
	}

	@Override
	public String toString() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM HH:mm");
		String startStr = start == null ? "null      " : fmt.print(start);
		String endStr = end == null ? "null      " : fmt.print(end);
		return "\n         PlannedSurgery {" +
				"surgery=" + surgery +
				"allocation=" + previousTimetableEntry +
				//				", start=" + startStr +
				//				", end=" + endStr +
				//				", location=" + location +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PlannedSurgery that = (PlannedSurgery) o;

		if (end != null ? !end.equals(that.end) : that.end != null) {
			return false;
		}
		if (start != null ? !start.equals(that.start) : that.start != null) {
			return false;
		}
		if (surgery != null ? !surgery.equals(that.surgery) : that.surgery != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = surgery != null ? surgery.hashCode() : 0;
		result = 31 * result + (start != null ? start.hashCode() : 0);
		result = 31 * result + (end != null ? end.hashCode() : 0);
		return result;
	}

	/**
	 * updates the begin and finish times of the surgery object
	 * and stores it in the db
	 *
	 * @param service
	 * @should update schedulingData object and persist it into the db if location is not null
	 * @should set set start end and location fields to null if location is null
	 */
	public void persist(OperationTheaterService service) {
		SchedulingData scheduling = surgery.getSchedulingData();
		if (scheduling == null) {
			scheduling = new SchedulingData();
			surgery.setSchedulingData(scheduling);
		}
		if (location != null) {
			scheduling.setStart(start);
			scheduling.setEnd(end);
			scheduling.setLocation(location);

		} else {
			scheduling.setStart(null);
			scheduling.setEnd(null);
			scheduling.setLocation(null);
		}
		service.saveSurgery(surgery);
	}
}
