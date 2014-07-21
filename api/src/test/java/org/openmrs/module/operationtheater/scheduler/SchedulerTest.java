package org.openmrs.module.operationtheater.scheduler;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.operation.Operation;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonTokenUtil;
import org.openmrs.module.operationtheater.DbUtil;
import org.openmrs.module.operationtheater.DbUtilDefaultInserts;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;

import java.util.List;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
						.values(refDate.plusHours(50).toDate(), refDate.plusHours(50).plusMinutes(35 + 25).toDate(), 100,
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

		List<Surgery> surgeries = otService.getAllSurgeries(false);
		//FIXME add checks for surgery #1 and #2
		assertThat(surgeries.get(2).getSchedulingData().getStart(), equalTo(refDate.plusHours(50)));
		assertThat(surgeries.get(2).getSchedulingData().getLocation().getId(), is(100));
	}

	/**
	 * @verifies throw IllegalStateException if solve has already been started but not finished
	 * @see Scheduler#solve()
	 */
	@Test(expected = IllegalStateException.class)
	public void solve_shouldThrowIllegalStateExceptionIfSolveHasAlreadyBeenStartedButNotFinished() throws Exception {
		Scheduler.INSTANCE.setStatus(Scheduler.Status.RUNNING);

		//call method under test
		Scheduler.INSTANCE.solve();
	}
}
