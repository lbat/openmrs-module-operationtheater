package org.openmrs.module.operationtheater.scheduler;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.operation.Operation;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonTokenUtil;
import org.openmrs.module.operationtheater.DbUtil;
import org.openmrs.module.operationtheater.DbUtilDefaultInserts;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.Anchor;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;

import java.util.List;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.openmrs.module.operationtheater.DbUtil.Config;
import static org.openmrs.module.operationtheater.DbUtil.insertInto;

/**
 * Automatic scheduler integration test
 */
public class SchedulerTest extends BaseModuleContextSensitiveTest {

	private static DbSetupTracker dbSetupTracker = new DbSetupTracker();

	private DateTime refDate = new DateTime().withTimeAtStartOfDay();

	@Before
	public void setUp() throws Exception {
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Appendectomy", 35, 25, 4)
						.build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("start", "end", "location_id", "date_locked")
						.values(refDate.plusHours(36).toDate(), refDate.plusHours(36).plusMinutes(35 + 25).toDate(), 100,
								false)
						.values(refDate.plusHours(36).toDate(), refDate.plusHours(36).plusMinutes(35 + 25).toDate(), 100,
								false)
						.values(refDate.plusHours(60).toDate(), refDate.plusHours(60).plusMinutes(35 + 25).toDate(), 100,
								true)
						.build(),
				insertInto(Config.SURGERY, "date_created")
						.columns("patient_id", "procedure_id", "surgery_completed", "scheduling_data_id", "date_created")
						.values(100, 1, false, 1, refDate.minusWeeks(1).toDate())
						.values(100, 1, false, 2, refDate.minusWeeks(2).toDate())
						.values(100, 1, false, 3, refDate.minusWeeks(3).toDate())
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);

		DaemonTokenUtil.passDaemonTokenToModule();
	}

	@After
	public void tearDown() {
		//don't influence other tests
		Scheduler.INSTANCE.reset();
	}

	/**
	 * @verifies solve operation theater planning problem
	 * @see Scheduler#solve()
	 */
	@Test
	public void solve_shouldSolveOperationTheaterPlanningProblem() throws Exception {
		Time timeMock = Mockito.mock(Time.class);
		when(timeMock.now()).thenReturn(refDate.withTime(11, 0, 0, 0));
		Whitebox.setInternalState(Scheduler.INSTANCE, "time", timeMock);

		//modify solverFactory to come up with reproducible results
		SolverFactory solverFactory = Scheduler.INSTANCE.getSolverFactory();
		solverFactory.getSolverConfig().setEnvironmentMode(EnvironmentMode.FAST_ASSERT);
		Scheduler.INSTANCE.setSolverFactory(solverFactory);

		//call method under test
		Scheduler.INSTANCE.solve();

		//wait until a solution has been found
		long max_waiting_time = 180000;//3min
		long start = System.currentTimeMillis();
		while (Scheduler.INSTANCE.getStatus() == Scheduler.Status.RUNNING
				&& System.currentTimeMillis() < start + max_waiting_time) {
			Thread.currentThread().sleep(2000); //2s
		}

		//verify
		OperationTheaterService otService = Context.getService(OperationTheaterService.class);

		assertThat(Scheduler.INSTANCE.getStatus(), is(Scheduler.Status.SUCCEEDED));
		List<Surgery> surgeries = otService.getAllSurgeries(false);
		int i = 0;
		assertThat(surgeries.get(i).getUuid(), is("surgery1"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withTime(11, 0, 0, 0)));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(100));
		i++;
		assertThat(surgeries.get(i).getUuid(), is("surgery2"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.withTime(11, 0, 0, 0)));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(101));
		i++;
		assertThat(surgeries.get(i).getUuid(), is("surgery3"));
		assertThat(surgeries.get(i).getSchedulingData().getStart(), equalTo(refDate.plusHours(60)));
		assertThat(surgeries.get(i).getSchedulingData().getLocation().getId(), is(100));
	}

	/**
	 * @verifies throw IllegalStateException if solve has already been started but not finished
	 * @see Scheduler#solve()
	 */
	@Test(expected = IllegalStateException.class)
	public void solve_shouldThrowIllegalStateExceptionIfSolveHasAlreadyBeenStartedButNotFinished() throws Exception {
		Whitebox.setInternalState(Scheduler.INSTANCE, "status", Scheduler.Status.RUNNING);

		//call method under test
		Scheduler.INSTANCE.solve();
	}

	/**
	 * @verifies properly setup the initial solution
	 * @see Scheduler#setupInitialSolution(int)
	 */
	@Test
	public void setupInitialSolution_shouldProperlySetupTheInitialSolution() throws Exception {
		OperationTheaterService otService = Context.getService(OperationTheaterService.class);
		LocationService locationService = Context.getLocationService();
		Time timeMock = Mockito.mock(Time.class);
		when(timeMock.now()).thenReturn(refDate.withTime(11, 0, 0, 0));

		Whitebox.setInternalState(Scheduler.INSTANCE, "time", timeMock);
		Whitebox.setInternalState(Scheduler.INSTANCE, "otService", otService);
		Whitebox.setInternalState(Scheduler.INSTANCE, "locationService", locationService);

		//call method under test
		Timetable timetable = Scheduler.INSTANCE.setupInitialSolution(2);

		//verify
		//anchors
		List<Anchor> anchors = timetable.getAnchors();
		assertThat(anchors, hasSize(5));
		int i = 0;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart().withMillis(0), equalTo(new DateTime().withMillis(0)));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(new DateTime().plusDays(1).withTime(8, 0, 0, 0)));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 1"));
		assertThat(anchors.get(i).getStart(), equalTo(new DateTime().plusDays(2).withTime(12, 0, 0, 0)));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 2"));
		assertThat(anchors.get(i).getStart().withMillis(0), equalTo(new DateTime().withMillis(0)));
		i++;
		assertThat(anchors.get(i).getLocation().getName(), is("OT 2"));
		assertThat(anchors.get(i).getStart(), equalTo(new DateTime().plusDays(1).withTime(8, 0, 0, 0)));

		//planned surgeries
		List<PlannedSurgery> plannedSurgeries = timetable.getPlannedSurgeries();
		assertThat(plannedSurgeries, hasSize(3));
		i = 0;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery1"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(refDate.plusDays(1).plusHours(8))); //start time is not the same as in db - determined by anchor
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusDays(1).plusHours(9)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) anchors.get(1)));
		i++;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery2"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(),
				is(refDate.plusDays(1).plusHours(9))); //start time is not the same as in db - determined by previous element
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusDays(1).plusHours(10)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) plannedSurgeries.get(0)));
		i++;
		assertThat(plannedSurgeries.get(i).getSurgery().getUuid(), is("surgery3"));
		assertThat(plannedSurgeries.get(i).getLocation().getName(), is("OT 1"));
		assertThat(plannedSurgeries.get(i).getStart(), is(refDate.plusHours(60)));
		assertThat(plannedSurgeries.get(i).getEnd(), is(refDate.plusHours(61)));
		assertThat(plannedSurgeries.get(i).getPreviousTimetableEntry(), is((TimetableEntry) anchors.get(2)));
	}
}
