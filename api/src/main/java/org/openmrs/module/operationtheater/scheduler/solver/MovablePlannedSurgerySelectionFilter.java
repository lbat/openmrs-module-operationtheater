package org.openmrs.module.operationtheater.scheduler.solver;

import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MovablePlannedSurgerySelectionFilter implements SelectionFilter<PlannedSurgery> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, PlannedSurgery surgery) {
		SchedulingData schedulingData = surgery.getSurgery().getSchedulingData();
		return schedulingData == null ? true : !schedulingData.getDateLocked();
	}
}
