package org.openmrs.module.operationtheater.scheduler.solver;

import org.junit.Test;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

/**
 * Tests {@link PlannedSurgeryDifficultyComparator}
 */
public class PlannedSurgeryDifficultyComparatorTest {

	/**
	 * @verifies return a positive number if left is more difficult than right
	 * @see PlannedSurgeryDifficultyComparator#compare(org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery, org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery)
	 */
	@Test
	public void compare_shouldReturnAPositiveNumberIfLeftIsMoreDifficultThanRight() throws Exception {
		Procedure left = new Procedure();
		left.setInterventionDuration(60);
		left.setOtPreparationDuration(10);
		Procedure right = new Procedure();
		right.setInterventionDuration(30);
		right.setOtPreparationDuration(10);

		Surgery surgeryLeft = new Surgery();
		surgeryLeft.setProcedure(left);
		Surgery surgeryRight = new Surgery();
		surgeryRight.setProcedure(right);

		PlannedSurgery plannedSurgeryLeft = new PlannedSurgery();
		plannedSurgeryLeft.setSurgery(surgeryLeft);

		PlannedSurgery plannedSurgeryRight = new PlannedSurgery();
		plannedSurgeryRight.setSurgery(surgeryRight);

		//call method under test
		int result = new PlannedSurgeryDifficultyComparator().compare(plannedSurgeryLeft, plannedSurgeryRight);

		//verify
		assertThat(result, greaterThan(0));
	}

	/**
	 * @verifies return a negative number if right is more difficult than left
	 * @see PlannedSurgeryDifficultyComparator#compare(org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery, org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery)
	 */
	@Test
	public void compare_shouldReturnANegativeNumberIfRightIsMoreDifficultThanLeft() throws Exception {
		Procedure left = new Procedure();
		left.setInterventionDuration(30);
		left.setOtPreparationDuration(10);
		Procedure right = new Procedure();
		right.setInterventionDuration(60);
		right.setOtPreparationDuration(10);

		Surgery surgeryLeft = new Surgery();
		surgeryLeft.setProcedure(left);
		Surgery surgeryRight = new Surgery();
		surgeryRight.setProcedure(right);

		PlannedSurgery plannedSurgeryLeft = new PlannedSurgery();
		plannedSurgeryLeft.setSurgery(surgeryLeft);

		PlannedSurgery plannedSurgeryRight = new PlannedSurgery();
		plannedSurgeryRight.setSurgery(surgeryRight);

		//call method under test
		int result = new PlannedSurgeryDifficultyComparator().compare(plannedSurgeryLeft, plannedSurgeryRight);

		//verify
		assertThat(result, lessThan(0));
	}

	/**
	 * @verifies return zero if both objects are equally difficult
	 * @see PlannedSurgeryDifficultyComparator#compare(org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery, org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery)
	 */
	@Test
	public void compare_shouldReturnZeroIfBothObjectsAreEquallyDifficult() throws Exception {
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(60);
		procedure.setOtPreparationDuration(10);

		Surgery surgery = new Surgery();
		surgery.setProcedure(procedure);

		PlannedSurgery plannedSurgeryLeft = new PlannedSurgery();
		plannedSurgeryLeft.setSurgery(surgery);

		PlannedSurgery plannedSurgeryRight = new PlannedSurgery();
		plannedSurgeryRight.setSurgery(surgery);

		//call method under test
		int result = new PlannedSurgeryDifficultyComparator().compare(plannedSurgeryLeft, plannedSurgeryRight);

		//verify
		assertThat(result, is(0));
	}
}
