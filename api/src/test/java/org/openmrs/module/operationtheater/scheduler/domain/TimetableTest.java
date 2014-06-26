package org.openmrs.module.operationtheater.scheduler.domain;

import org.joda.time.DateTime;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Surgery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Timetable}
 */
public class TimetableTest {

	/**
	 * @verifies return all problem facts without the planning entities
	 * @see Timetable#getProblemFacts()
	 */
	@Test
	public void getProblemFacts_shouldReturnAllProblemFactsWithoutThePlanningEntities() throws Exception {
		Timetable timetable = new Timetable();

		Surgery s1 = new Surgery();
		Surgery s2 = new Surgery();
		List<Surgery> surgeries = new ArrayList<Surgery>();
		surgeries.add(s1);
		surgeries.add(s2);

		Location l1 = new Location();
		Location l2 = new Location();
		List<Location> locations = new ArrayList<Location>();
		locations.add(l1);
		locations.add(l2);

		DateTime t1 = new DateTime();
		DateTime t2 = new DateTime();
		List<DateTime> startTimes = new ArrayList<DateTime>();
		startTimes.add(t1);
		startTimes.add(t2);

		timetable.setSurgeries(surgeries);
		timetable.setLocations(locations);
		timetable.setStartTimes(startTimes);

		PlannedSurgery ps1 = new PlannedSurgery();
		PlannedSurgery ps2 = new PlannedSurgery();
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		plannedSurgeries.add(ps1);
		plannedSurgeries.add(ps2);

		timetable.setPlannedSurgeries(plannedSurgeries);

		//call method under test
		Collection<?> problemFacts = timetable.getProblemFacts();

		//verify contains all problem facts
		assertThat(problemFacts, hasSize(6));
		assertTrue(problemFacts.containsAll(surgeries));
		assertTrue(problemFacts.containsAll(locations));
		assertTrue(problemFacts.containsAll(startTimes));

		//verify contains NO planning entities
		assertFalse(problemFacts.contains(ps1));
		assertFalse(problemFacts.contains(ps2));
	}

}
