package org.openmrs.module.operationtheater.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.OperationTheaterModuleActivator;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.XmlSolverFactory;

import java.util.ArrayList;
import java.util.List;

public enum Scheduler {

	INSTANCE;

	protected Log log = LogFactory.getLog(getClass());

	private SolverFactory solverFactory;

	private OperationTheaterService otService;

	private LocationService locationService;

	private Solver solver;

	private Thread solverThread;

	private Status status = Status.PRISTINE;

	private Scheduler() {
		solverFactory = new XmlSolverFactory("/scheduler/solverConfig.xml");
	}

	/**
	 * used for testing purposes not during normal execution
	 * reset class after each test to avoid influence on subsequent tests
	 *
	 * @param
	 */
	public void reset() {
		solverFactory = new XmlSolverFactory("/scheduler/solverConfig.xml");
		solver = null;
		solverThread = null;
		status = Status.PRISTINE;
	}

	/**
	 * used for testing purposes not during normal execution
	 */
	public SolverFactory getSolverFactory() {
		return solverFactory;
	}

	/**
	 * used for testing purposes not during normal execution
	 *
	 * @param solverFactory
	 */
	public void setSolverFactory(SolverFactory solverFactory) {
		this.solverFactory = solverFactory;
	}

	/**
	 * solves the operation theater planning problem
	 * non blocking (solver runs in new thread)
	 * use isSolving method to determine if solving process has already finished
	 *
	 * @should solve operation theater planning problem
	 * @should throw IllegalStateException if solve has already been started but not finished
	 */
	//FIXME add privilege level
	public void solve() throws IllegalStateException {
		if (status == Status.RUNNING) {
			throw new IllegalStateException("Already solving");
		}
		if (solver == null) {
			solver = solverFactory.buildSolver();
		}

		status = Status.RUNNING;

		solverThread = Daemon.runInDaemonThread(new Runnable() {

			@Override
			public void run() {
				try {
					otService = Context.getService(OperationTheaterService.class);
					locationService = Context.getLocationService();

					// Load problem
					final Timetable unsolvedTimetable = createTimetable();

					// Solve problem
					solver.setPlanningProblem(unsolvedTimetable);
					solver.solve();
					Timetable solvedTimetable = (Timetable) solver.getBestSolution();

					//set surgeries planned begin and finished attributes
					solvedTimetable.persistSolution(otService);

					status = Status.SUCCEEDED;

					// Display the result
					System.out.println("\nsolved timetable\n"
							+ solvedTimetable);
				}
				catch (Exception e) {
					status = Status.FAILED;
					log.error(e);
				}
			}
		}, OperationTheaterModuleActivator.DAEMON_TOKEN);
	}

	/**
	 * returns if solver is still solving the planning problem
	 *
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * used for testing purposes not during normal execution
	 *
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	private Timetable createTimetable() {
		Timetable timetable = new Timetable();

		//get all uncompleted surgeries
		List<Surgery> surgeries = otService.getAllUncompletedSurgeries();

		//get operation theaters
		LocationTag tag = locationService.getLocationTagByUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		List<Location> locations = locationService.getLocationsByTag(tag);
		for (Location location : locations) {
			System.out.println(location.getName());
		}

		//generate possible surgery start dates
		DateTime timestamp = new DateTime();
		DateTime end = timestamp.plusDays(1);
		List<DateTime> startTimes = new ArrayList<DateTime>();
		for (; timestamp.isBefore(end); timestamp = timestamp.plusMinutes(60)) {//TODO specify in system property
			//			System.out.println(timestamp);
			startTimes.add(timestamp);
		}

		//set up initial solution
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		for (Surgery surgery : surgeries) {
			PlannedSurgery plannedSurgery = new PlannedSurgery();
			plannedSurgery.setSurgery(surgery);
			SchedulingData scheduling = surgery.getSchedulingData();
			if (scheduling != null && scheduling.getStart() != null) {
				plannedSurgery.setStart(scheduling.getStart());
				plannedSurgery.setEnd(scheduling.getEnd());
				plannedSurgery.setLocation(scheduling.getLocation());
			}
			plannedSurgeries.add(plannedSurgery);
		}

		//set planning facts
		timetable.setSurgeries(surgeries);
		timetable.setLocations(locations);
		timetable.setStartTimes(startTimes);

		//set planning entity
		timetable.setPlannedSurgeries(plannedSurgeries);
		return timetable;
	}

	public static enum Status {
		PRISTINE, RUNNING, SUCCEEDED, FAILED
	}
}
