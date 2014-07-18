package org.openmrs.module.operationtheater.scheduler.solver;

import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Created by lukas on 16.07.14.
 */
public class MovablePlannedSurgerySelectionFilter implements SelectionFilter<PlannedSurgery> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, PlannedSurgery surgery) {
		return !surgery.getSurgery().getSchedulingData().getDateLocked();
	}
}
