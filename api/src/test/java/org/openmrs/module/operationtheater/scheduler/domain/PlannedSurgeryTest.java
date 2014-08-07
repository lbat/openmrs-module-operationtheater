package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		ps2.setStart(ps1.getEnd());

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
	 * @verifies update schedulingData object and persist it into the db if location is not null
	 * @see PlannedSurgery#persist(org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void persist_shouldUpdateSchedulingDataObjectAndPersistItIntoTheDbIfLocationIsNotNull() throws Exception {
		OperationTheaterService mock = Mockito.mock(OperationTheaterService.class);

		Surgery surgery = new Surgery();
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		plannedSurgery.setSurgery(surgery);
		plannedSurgery.setStart(new DateTime(), false);
		plannedSurgery.setEnd(new DateTime().plusHours(2));
		plannedSurgery.setLocation(new Location());

		//call function under test
		plannedSurgery.persist(mock);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(mock).saveSurgery(captor.capture());

		assertNotNull(captor);
		SchedulingData result = captor.getValue().getSchedulingData();
		assertThat(result.getStart(), is(plannedSurgery.getStart()));
		assertThat(result.getEnd(), is(plannedSurgery.getEnd()));
		assertThat(result.getLocation(), is(plannedSurgery.getLocation()));
	}

	/**
	 * @verifies set set start end and location fields to null if location is null
	 * @see PlannedSurgery#persist(org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void persist_shouldSetSetStartEndAndLocationFieldsToNullIfLocationIsNull() throws Exception {
		OperationTheaterService mock = Mockito.mock(OperationTheaterService.class);

		SchedulingData schedulingData = new SchedulingData();
		schedulingData.setLocation(new Location());
		schedulingData.setStart(new DateTime());
		schedulingData.setEnd(new DateTime().plusHours(1));

		PlannedSurgery plannedSurgery = new PlannedSurgery();
		plannedSurgery.setLocation(null);
		plannedSurgery.setSurgery(new Surgery());

		//call method under test
		plannedSurgery.persist(mock);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(mock).saveSurgery(captor.capture());

		//verify
		SchedulingData result = captor.getValue().getSchedulingData();
		assertThat(result.getStart(), is(nullValue()));
		assertThat(result.getEnd(), is(nullValue()));
		assertThat(result.getLocation(), is(nullValue()));
	}

	/**
	 * @verifies return true if location start or end variables are null
	 * @see PlannedSurgery#isOutsideAvailableTimes()
	 */
	@Test
	public void isOutsideAvailableTimes_shouldReturnTrueIfLocationStartOrEndVariablesAreNull() throws Exception {
		PlannedSurgery surgery = new PlannedSurgery();
		surgery.setStart(new DateTime(), false);
		surgery.setEnd(new DateTime());

		//call function under test
		boolean result = surgery.isOutsideAvailableTimes();

		//verify
		assertThat(result, is(true));

		surgery.setLocation(new Location());
		surgery.setStart(null, false);

		//call function under test
		result = surgery.isOutsideAvailableTimes();

		//verify
		assertThat(result, is(true));

		surgery.setStart(new DateTime(), false);
		surgery.setEnd(null);

		//call function under test
		result = surgery.isOutsideAvailableTimes();

		//verify
		assertThat(result, is(true));
	}

	/**
	 * @verifies return if current scheduling is outside available times
	 * @see PlannedSurgery#isOutsideAvailableTimes()
	 */
	@Test
	public void isOutsideAvailableTimes_shouldReturnIfCurrentSchedulingIsOutsideAvailableTimes()
			throws Exception {
		OperationTheaterService mockedService = Mockito.mock(OperationTheaterService.class);
		Interval interval = new Interval(new DateTime(), new DateTime().plusHours(5));
		when(mockedService.getLocationAvailableTime(any(Location.class), any(DateTime.class))).thenReturn(interval);

		PlannedSurgery surgery = new PlannedSurgery();
		surgery.setLocation(new Location());
		Whitebox.setInternalState(surgery, "otService", mockedService);

		surgery.setStart(new DateTime().minusHours(1), false);
		surgery.setEnd(new DateTime().plusHours(1));

		//call function under test
		boolean result = surgery.isOutsideAvailableTimes();

		//verify
		assertThat(result, is(true));

		surgery.setStart(new DateTime().plusMinutes(30), false);
		surgery.setEnd(interval.getEnd());

		//call function under test
		result = surgery.isOutsideAvailableTimes();

		//verify
		assertThat(result, is(false));
	}

	/**
	 * @verifies return the entire duration this surgery occupies the ot when nextTimeTableEntry is null
	 * @see PlannedSurgery#getChainLengthInMinutes()
	 */
	@Test
	public void getChainLengthInMinutes_shouldReturnTheEntireDurationThisSurgeryOccupiesTheOtWhenNextTimeTableEntryIsNull()
			throws Exception {
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(33);
		procedure.setOtPreparationDuration(14);
		surgery.setProcedure(procedure);
		plannedSurgery.setSurgery(surgery);

		plannedSurgery.setNextTimetableEntry(null);

		//call method under test
		int result = plannedSurgery.getChainLengthInMinutes();

		//verify
		assertThat(result, is(47));
	}

	/**
	 * @verifies return value from its successor in the chain added to the entire duration this surgery occupies the ot
	 * @see PlannedSurgery#getChainLengthInMinutes()
	 */
	@Test
	public void getChainLengthInMinutes_shouldReturnValueFromItsSuccessorInTheChainAddedToTheEntireDurationThisSurgeryOccupiesTheOt()
			throws Exception {
		PlannedSurgery mock = Mockito.mock(PlannedSurgery.class);
		when(mock.getChainLengthInMinutes()).thenReturn(120);

		PlannedSurgery ps = new PlannedSurgery();
		Surgery surgery = new Surgery();
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(60);
		procedure.setOtPreparationDuration(20);
		surgery.setProcedure(procedure);
		ps.setSurgery(surgery);

		Whitebox.setInternalState(ps, "nextTimetableEntry", mock);

		//call method under test
		int result = ps.getChainLengthInMinutes();

		//verify
		assertThat(result, is(200));
	}
}
