package org.openmrs.module.operationtheater.scheduler.solver;

import org.joda.time.DateTime;
import org.junit.Test;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests {@link MovablePlannedSurgerySelectionFilter}
 */
public class MovablePlannedSurgerySelectionFilterTest {

	/**
	 * @verifies return true if schedulingData is null and surgery hasn't started yet
	 * @see MovablePlannedSurgerySelectionFilter#accept(ScoreDirector, PlannedSurgery)
	 */
	@Test
	public void accept_shouldReturnTrueIfSchedulingDataIsNullAndSurgeryHasntStartedYet() throws Exception {
		//prepare
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		plannedSurgery.setSurgery(surgery);

		//call method under test
		boolean result = new MovablePlannedSurgerySelectionFilter().accept(null, plannedSurgery);

		//verify
		assertThat(result, is(true));
	}

	/**
	 * @verifies return false if surgery has been started
	 * @see MovablePlannedSurgerySelectionFilter#accept(ScoreDirector, PlannedSurgery)
	 */
	@Test
	public void accept_shouldReturnFalseIfSurgeryHasBeenStarted() throws Exception {
		//prepare
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		surgery.setDateStarted(new DateTime());
		plannedSurgery.setSurgery(surgery);

		//call method under test
		boolean result = new MovablePlannedSurgerySelectionFilter().accept(null, plannedSurgery);

		//verify
		assertThat(result, is(false));
	}

	/**
	 * @verifies return false if surgery has been locked
	 * @see MovablePlannedSurgerySelectionFilter#accept(ScoreDirector, PlannedSurgery)
	 */
	@Test
	public void accept_shouldReturnFalseIfSurgeryHasBeenLocked() throws Exception {
		//prepare
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		SchedulingData schedulingData = new SchedulingData();
		schedulingData.setDateLocked(true);
		plannedSurgery.setSurgery(surgery);

		//call method under test
		boolean result = new MovablePlannedSurgerySelectionFilter().accept(null, plannedSurgery);

		//verify
		assertThat(result, is(true));
	}
}
