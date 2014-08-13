package org.openmrs.module.operationtheater.scheduler.solver;

import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MovablePlannedSurgerySelectionFilter implements SelectionFilter<PlannedSurgery> {

	/**
	 * returns false if surgery has been started or locked<br />
	 * prevents that this plannedSurgery is moved to another operation theater or start time
	 *
	 * @param scoreDirector
	 * @param surgery
	 * @return
	 * @should return true if schedulingData is null and surgery hasn't started yet
	 * @should return false if surgery has been started
	 * @should return false if surgery has been locked
	 */
	@Override
	public boolean accept(ScoreDirector scoreDirector, PlannedSurgery surgery) {
		SchedulingData schedulingData = surgery.getSurgery().getSchedulingData();
		boolean started = surgery.getSurgery().getDateStarted() != null;
		boolean locked = schedulingData == null ? false : schedulingData.getDateLocked();
		return !started && !locked;
	}
}
