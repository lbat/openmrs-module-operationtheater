package org.openmrs.module.operationtheater.scheduler.solver;

import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;

import java.util.Comparator;

/**
 * This class is used to determine the difficulty of a surgery
 * Difficult surgeries should be scheduled first in order to increase efficiency
 * In this case difficulty is the overall duration of a surgery
 * (it is easier to schedule a very long surgery at the beginning than at the end of
 * the process if only a few slots are left)
 */
public class PlannedSurgeryDifficultyComparator implements Comparator<PlannedSurgery> {

	/**
	 * @param left
	 * @param right
	 * @return
	 * @should return a positive number if left is more difficult than right
	 * @should return a negative number if right is more difficult than left
	 * @should return zero if both objects are equally difficult
	 */
	@Override
	public int compare(PlannedSurgery left, PlannedSurgery right) {
		Procedure procedureLeft = left.getSurgery().getProcedure();
		Procedure procedureRight = right.getSurgery().getProcedure();

		int difficultyLeft = procedureLeft.getInterventionDuration() + procedureLeft.getOtPreparationDuration();
		int difficultyRight = procedureRight.getInterventionDuration() + procedureRight.getOtPreparationDuration();

		return difficultyLeft - difficultyRight; //ascending (positive if left > right)
	}
}
