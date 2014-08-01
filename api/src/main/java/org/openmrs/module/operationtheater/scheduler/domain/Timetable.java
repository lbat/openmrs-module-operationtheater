package org.openmrs.module.operationtheater.scheduler.domain;

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
public class Timetable implements Solution<HardSoftScore> {

	//problem facts  (don't change value during planning)
	private List<Anchor> anchors;

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
		facts.addAll(anchors);
		return facts;
	}

	@Override
	public String toString() {
		return "Timetable{" +
				"plannedSurgeries=" + plannedSurgeries +
				", score=" + score +
				'}';
	}

	@ValueRangeProvider(id = "anchorRange")
	public List<Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(List<Anchor> anchors) {
		this.anchors = anchors;
	}

	@PlanningEntityCollectionProperty
	@ValueRangeProvider(id = "plannedSurgeryRange")
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
