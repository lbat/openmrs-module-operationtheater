package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link PlannedSurgery}
 */
public class PlannedSurgeryTest {

	/**
	 * @verifies set start and update end accordingly
	 * @see PlannedSurgery#setStart(DateTime)
	 */
	@Test
	public void setStart_shouldSetStartAndUpdateEndAccordingly() throws Exception {

		//set up surgery and procedure objects are needed to calculate end time in setStart method
		Surgery surgery = createSurgery();

		PlannedSurgery plannedSurgery = new PlannedSurgery();
		plannedSurgery.setSurgery(surgery);

		DateTime now = new DateTime();
		DateTime expectedEnd = now.plusMinutes(123 + 45);

		//call function under test
		plannedSurgery.setStart(now);

		assertThat(plannedSurgery.getStart(), is(now));
		assertThat(plannedSurgery.getEnd(), equalTo(expectedEnd));
	}

	/**
	 * @verifies return if the two intervals overlap
	 * @see PlannedSurgery#isOverlapping(PlannedSurgery)
	 */
	@Test
	public void isOverlapping_shouldReturnIfTheTwoIntervalsOverlap() throws Exception {
		DateTime start = new DateTime();
		Surgery surgery = createSurgery();

		PlannedSurgery ps1 = new PlannedSurgery();
		ps1.setSurgery(surgery);
		PlannedSurgery ps2 = new PlannedSurgery();
		ps2.setSurgery(surgery);

		//intervals overlap
		ps1.setStart(start);
		ps2.setStart(start);

		//call function under test;
		assertTrue(ps1.isOverlapping(ps2));
		assertTrue(ps2.isOverlapping(ps1));

		//intervals do not overlap
		DateTime startTomorrow = start.plusDays(1);
		ps2.setStart(startTomorrow);

		//call function under test;
		assertFalse(ps1.isOverlapping(ps2));
		assertFalse(ps2.isOverlapping(ps1));
	}

	/**
	 * @verifies return false if any of the date object are null
	 * @see PlannedSurgery#isOverlapping(PlannedSurgery)
	 */
	@Test
	public void isOverlapping_shouldReturnFalseIfAnyOfTheDateObjectAreNull() throws Exception {
		DateTime start = new DateTime();
		DateTime end = start.plusMinutes(5);

		Surgery surgery = createSurgery();

		PlannedSurgery ps1 = new PlannedSurgery();
		ps1.setSurgery(surgery);
		PlannedSurgery ps2 = new PlannedSurgery();
		ps2.setSurgery(surgery);

		//end is null
		ps1.setStart(start);
		ps1.setEnd(end);
		ps2.setStart(start);
		ps2.setEnd(null);

		//call function under test;
		assertFalse(ps1.isOverlapping(ps2));
		assertFalse(ps2.isOverlapping(ps1));

		//start is null
		ps1.setStart(start);
		ps1.setEnd(end);
		ps2.setStart(null);
		ps2.setEnd(end);

		//call function under test;
		assertFalse(ps1.isOverlapping(ps2));
		assertFalse(ps2.isOverlapping(ps1));
	}

	/**
	 * set up surgery and procedure  - this is needed to calculate end time inside
	 * {@link PlannedSurgery#setStart(DateTime)} method
	 *
	 * @return
	 */
	private Surgery createSurgery() {
		Surgery surgery = new Surgery();
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(123);
		procedure.setOtPreparationDuration(45);
		surgery.setProcedure(procedure);
		return surgery;
	}

	/**
	 * @verifies updates the begin and finish times of the surgery object and stores it in the db
	 * @see PlannedSurgery#persist(org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void persist_shouldUpdatesTheBeginAndFinishTimesOfTheSurgeryObjectAndStoresItInTheDb() throws Exception {
		OperationTheaterService mock = Mockito.mock(OperationTheaterService.class);

		Surgery surgery = new Surgery();
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		plannedSurgery.setSurgery(surgery);
		plannedSurgery.setStart(new DateTime(), false);
		plannedSurgery.setEnd(new DateTime().plusHours(2));

		//call function under test
		plannedSurgery.persist(mock);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(mock).saveSurgery(captor.capture());

		assertNotNull(captor);
		assertThat(captor.getValue().getDatePlannedBegin(), is(plannedSurgery.getStart()));
		assertThat(captor.getValue().getDatePlannedFinish(), is(plannedSurgery.getEnd()));
	}
}
