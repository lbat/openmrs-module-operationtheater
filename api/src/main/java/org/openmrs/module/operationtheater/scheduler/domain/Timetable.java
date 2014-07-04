package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.value.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.solution.Solution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the planning solution
 * It contains all required objects for the solving process
 * Also used to act as a range provider for the planning variables
 */
@PlanningSolution
public class
		Timetable implements Solution<HardSoftScore> {

	//problem facts  (don't change value during planning)
	private List<Surgery> surgeries;

	private List<Location> locations; // these are the operation theaters

	private List<DateTime> startTimes;

	//planning entities
	private List<PlannedSurgery> plannedSurgeries;

	private HardSoftScore score;

	@Override
	public HardSoftScore getScore() {
		return score;
	}

	@Override
	public void setScore(HardSoftScore hardSoftScore) {
		score = hardSoftScore;
	}

	/**
	 * @return a collection with all problem facts
	 * @should return all problem facts without the planning entities
	 */
	@Override
	public Collection<?> getProblemFacts() {
		//planning entities are added automatically -> don't add them here
		List<Object> facts = new ArrayList<Object>();
		facts.addAll(surgeries);
		facts.addAll(locations);
		facts.addAll(startTimes);
		return facts;
	}

	@Override
	public String toString() {
		return "Timetable{" +
				"plannedSurgeries=" + plannedSurgeries +
				", score=" + score +
				'}';
	}

	public List<Surgery> getSurgeries() {
		return surgeries;
	}

	public void setSurgeries(List<Surgery> surgeries) {
		this.surgeries = surgeries;
	}

	@ValueRangeProvider(id = "startDateRange")
	public List<DateTime> getStartTimes() {
		return startTimes;
	}

	public void setStartTimes(List<DateTime> startTimes) {
		this.startTimes = startTimes;
	}

	@ValueRangeProvider(id = "locationRange")
	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	@PlanningEntityCollectionProperty
	public List<PlannedSurgery> getPlannedSurgeries() {
		return plannedSurgeries;
	}

	public void setPlannedSurgeries(List<PlannedSurgery> plannedSurgeries) {
		this.plannedSurgeries = plannedSurgeries;
	}

	/**
	 * invoke persist method of all planned surgery objects
	 *
	 * @param service
	 * @should invoke persist method of all planned surgery objects
	 */
	public void persistSolution(OperationTheaterService service) {
		for (PlannedSurgery plannedSurgery : plannedSurgeries) {
			plannedSurgery.persist(service);
		}
	}
}
