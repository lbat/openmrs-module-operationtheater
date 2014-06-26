package org.openmrs.module.operationtheater.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Created by lukas on 24.06.14.
 */
public class SchedulerTest extends BaseModuleContextSensitiveTest {

	@Before
	public void setUp() throws Exception {
		executeDataSet("standardOperationTheaterTestDataset.xml");
	}

	/**
	 * @verifies solve
	 * @see Scheduler#solve()
	 */
	@Test
	public void solve_shouldSolve() throws Exception {

		//		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		//		LocationService locationService = Mockito.mock(LocationService.class);
		//
		//		List<Surgery> surgeries = new ArrayList<Surgery>();
		//		surgeries.add(new Surgery());
		//		surgeries.add(new Surgery());
		//		surgeries.add(new Surgery());
		//		Mockito.when(otService.getAllSurgeries(false)).thenReturn(surgeries);
		//
		//		List<Location> locations = new ArrayList<Location>();
		//		locations.add(new Location(1));
		//		locations.add(new Location(2));
		//		locations.add(new Location(3));
		//		Mockito.when(locationService.getLocationsByTag(any(LocationTag.class))).thenReturn(locations);

		Scheduler scheduler = new Scheduler();
		//		scheduler.setOperationTheaterService(otService);
		//		scheduler.setLocationService(locationService);

		scheduler.solve();
	}
}
