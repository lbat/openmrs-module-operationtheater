package org.openmrs.module.operationtheater.scheduler.solver;

import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Created by lukas on 28.07.14.
 */
public class Filter implements SelectionFilter<PlannedSurgery> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, PlannedSurgery selection) {
		Timetable timetable = (Timetable) scoreDirector.getWorkingSolution();
		timetable.getPlannedSurgeries();
		return false;
	}
}
