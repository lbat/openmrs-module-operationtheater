package org.openmrs.module.operationtheater.scheduler.domain;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

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

		Anchor a1 = new Anchor(null, null);
		Anchor a2 = new Anchor(null, null);
		List<Anchor> anchors = new ArrayList<Anchor>();
		anchors.add(a1);
		anchors.add(a2);

		timetable.setAnchors(anchors);

		PlannedSurgery ps1 = new PlannedSurgery();
		PlannedSurgery ps2 = new PlannedSurgery();
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		plannedSurgeries.add(ps1);
		plannedSurgeries.add(ps2);

		timetable.setPlannedSurgeries(plannedSurgeries);

		//call method under test
		Collection<?> problemFacts = timetable.getProblemFacts();

		//verify contains all problem facts
		assertThat(problemFacts, hasSize(2));
		assertTrue(problemFacts.containsAll(anchors));

		//verify contains NO planning entities
		assertFalse(problemFacts.contains(ps1));
		assertFalse(problemFacts.contains(ps2));
	}

	/**
	 * @verifies invoke persist method of all planned surgery objects
	 * @see Timetable#persistSolution(org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void persistSolution_shouldInvokePersistMethodOfAllPlannedSurgeryObjects() throws Exception {
		Timetable timetable = new Timetable();

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);

		PlannedSurgery ps1 = Mockito.mock(PlannedSurgery.class);
		PlannedSurgery ps2 = Mockito.mock(PlannedSurgery.class);
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		plannedSurgeries.add(ps1);
		plannedSurgeries.add(ps2);

		timetable.setPlannedSurgeries(plannedSurgeries);

		//call method under test
		timetable.persistSolution(service);

		verify(ps1).persist(service);
		verify(ps2).persist(service);
	}
}
