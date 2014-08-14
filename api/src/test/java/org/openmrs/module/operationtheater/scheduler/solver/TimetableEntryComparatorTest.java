package org.openmrs.module.operationtheater.scheduler.solver;

import org.joda.time.DateTime;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests {@link TimetableEntryComparator}
 */
public class TimetableEntryComparatorTest {

	private DateTime refDate = new DateTime().withTime(10, 0, 0, 0);

	/**
	 * @verifies sorting PlannedSurgeries based on this Comparator should sort by location asc isStarted desc and start time asc
	 * @see TimetableEntryComparator#compare(org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry, org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry)
	 */
	@Test
	public void compare_shouldSortingPlannedSurgeriesBasedOnThisComparatorShouldSortByLocationAscIsStartedDescAndStartTimeAsc()
			throws Exception {
		//prepare
		PlannedSurgery ps0 = createPlainPlannedSurgery("b", false, refDate);
		PlannedSurgery ps1 = createPlainPlannedSurgery("b", true, refDate.plusHours(1));
		PlannedSurgery ps2 = createPlainPlannedSurgery("a", false, refDate);
		PlannedSurgery ps3 = createPlainPlannedSurgery("a", true, refDate.plusHours(1));

		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		plannedSurgeries.add(ps0);
		plannedSurgeries.add(ps1);
		plannedSurgeries.add(ps2);
		plannedSurgeries.add(ps3);

		//call method under test
		Collections.sort(plannedSurgeries, new TimetableEntryComparator());

		System.out.println(plannedSurgeries.indexOf(ps0));
		System.out.println(plannedSurgeries.indexOf(ps1));
		System.out.println(plannedSurgeries.indexOf(ps2));
		System.out.println(plannedSurgeries.indexOf(ps3));

		//verify
		assertThat(plannedSurgeries.get(0), is(ps3));
		assertThat(plannedSurgeries.get(1), is(ps2));
		assertThat(plannedSurgeries.get(2), is(ps1));
		assertThat(plannedSurgeries.get(3), is(ps0));

	}

	private PlannedSurgery createPlainPlannedSurgery(String locationUuid, boolean isStarted, DateTime scheduledStartTime) {
		PlannedSurgery plannedSurgery = new PlannedSurgery();
		SchedulingData schedulingData = new SchedulingData();
		schedulingData.setStart(scheduledStartTime);
		Surgery surgery = new Surgery();
		surgery.setSchedulingData(schedulingData);
		if (isStarted) {
			surgery.setDateStarted(refDate);
		}
		plannedSurgery.setSurgery(surgery);
		Location location = new Location();
		location.setUuid(locationUuid);
		plannedSurgery.setLocation(location);
		return plannedSurgery;
	}
}
