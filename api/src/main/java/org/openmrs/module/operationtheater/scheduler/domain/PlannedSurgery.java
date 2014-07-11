package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * This class is the Planning entity of this optimization problem
 * This means it contains attributes which will be changed by the solver during the solution finding process
 * This attributes are called Planning Variables - see the @PlanningVariable annotation on the corresponding setters
 * possible values of a planning variable is defined by the value range provider
 */
@PlanningEntity
public class PlannedSurgery {

	private Surgery surgery;

	// Planning variables: will change during planning (between score calculations)
	private Location location;

	private DateTime start;

	//shadow variable - automatically calculated if the underlying genuine planning variable (start) changes its value
	//not changed by the solver
	private DateTime end;

	public PlannedSurgery(Surgery surgery, DateTime start, DateTime end) {
		this.surgery = surgery;
		this.start = start;
		this.end = end;
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

	public Surgery getSurgery() {
		return surgery;
	}

	public void setSurgery(Surgery surgery) {
		this.surgery = surgery;
	}

	@PlanningVariable(valueRangeProviderRefs = { "locationRange" })
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@PlanningVariable(valueRangeProviderRefs = { "startDateRange" })
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

	@Override
	public String toString() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM HH:mm");
		String startStr = start == null ? "null      " : fmt.print(start);
		String endStr = end == null ? "null      " : fmt.print(end);
		return "\n         PlannedSurgery {" +
				"surgery=" + surgery +
				", start=" + startStr +
				", end=" + endStr +
				", location=" + location +
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
	 * @should update schedulingData object and store persist it into the db
	 */
	public void persist(OperationTheaterService service) {
		SchedulingData scheduling = surgery.getSchedulingData();
		if (scheduling == null) {
			scheduling = new SchedulingData();
			surgery.setSchedulingData(scheduling);
		}
		scheduling.setStart(start);
		scheduling.setEnd(end);
		scheduling.setLocation(location);
		service.saveSurgery(surgery);
	}
}
