package org.openmrs.module.operationtheater.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.OperationTheaterModuleActivator;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.Anchor;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;
import org.openmrs.module.operationtheater.scheduler.solver.TimetableEntryComparator;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.XmlSolverFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public enum Scheduler {

	INSTANCE;

	protected Log log = LogFactory.getLog(getClass());

	private Time time = new Time();

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
	 * @should solve 2 normal 1 locked and 1 outside planning period surgery
	 * @should solve 2 conflicting surgeries
	 * @should throw IllegalStateException if solve has already been started but not finished
	 */
	//FIXME add privilege level
	public void solve(final int planningWindow) throws IllegalStateException {
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
					final Timetable unsolvedTimetable = setupInitialSolution(planningWindow);

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
					e.printStackTrace();
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
	 * This function is responsible for building the initial solution object based on the last scheduling result and
	 * alterations.<br />
	 * operation theater scheduling is a continuous planning problem. This means that the input
	 * for the next planning is the last scheduling result (including deviations from the last result)
	 *
	 * @return
	 * @should properly setup the initial solution
	 * @should setup the initial solution if a surgery is currently performed
	 */
	Timetable setupInitialSolution(int planningWindowLength) {
		DateTime lastPlannedDay = time.now().plusDays(planningWindowLength);

		//get all uncompleted surgeries
		List<Surgery> surgeries = otService.getAllUncompletedSurgeries();

		//get operation theaters
		LocationTag tag = locationService.getLocationTagByUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		List<Location> locations = locationService.getLocationsByTag(tag);

		//generate chain anchors
		List<Anchor> anchors = getChainAnchors(surgeries, locations, lastPlannedDay);

		//set up plannedSurgeries chains
		List<PlannedSurgery> plannedSurgeries = setupPlannedSurgeryChains(surgeries, anchors, lastPlannedDay);

		return setupTimetable(anchors, plannedSurgeries);
	}

	/**
	 * optaplanner builds a chain of surgeries. each chain needs an anchor object. An Anchor has a location and a start date.
	 * As a result we need an anchor for each operation theater and the current time, as well as for each other day in the planning
	 * window. Additionally we also need an anchor for surgeries that have a fixed start time.
	 *
	 * @param surgeries
	 * @param locations
	 * @param lastPlannedDay
	 * @return
	 */
	private List<Anchor> getChainAnchors(List<Surgery> surgeries, List<Location> locations, DateTime lastPlannedDay) {
		List<Anchor> anchors = new ArrayList<Anchor>();

		//now
		DateTime now = time.now();
		for (Location location : locations) {
			Interval available = otService.getLocationAvailableTime(location, now);
			if (now.isBefore(available.getEnd())) {
				DateTime start = now.isAfter(available.getStart()) ? now : available.getStart();
				//if a surgery has already been started set anchor to the start time
				Surgery alreadyStarted = getStartedSurgery(surgeries, location);
				if (alreadyStarted != null) {
					start = alreadyStarted.getDateStarted();
				}
				anchors.add(new Anchor(location, start));
			}
		}

		//remaining planning period
		for (DateTime day = time.now().plusDays(1); day.isBefore(lastPlannedDay); day = day.plusDays(1)) {
			for (Location location : locations) {
				Interval available = otService.getLocationAvailableTime(location, day);
				anchors.add(new Anchor(location, available.getStart()));
			}
		}

		//fixed surgeries also need an anchor (also have to be inside the planning window)
		for (Surgery surgery : surgeries) {
			SchedulingData schedulingData = surgery.getSchedulingData();
			if (schedulingData.getDateLocked() && schedulingData.getStart().isBefore(lastPlannedDay)) {
				anchors.add(new Anchor(schedulingData.getLocation(), schedulingData.getStart()));
			}
		}

		if (anchors.size() == 0) {
			return anchors;
		}

		//calculate entire available time for an anchor (determines the maximum length of the chain)
		Collections.sort(anchors, new TimetableEntryComparator());
		ListIterator<Anchor> iterator = anchors.listIterator();
		Anchor anchor = iterator.next();
		Anchor next = anchor;
		while (next != null) {

			anchor = next;
			if (iterator.hasNext()) {
				next = iterator.next();
			} else {
				next = null;
			}

			DateTime end = null;
			if (next != null && anchor.getLocation().equals(next.getLocation()) && anchor.getStart().withTimeAtStartOfDay()
					.equals(next.getStart().withTimeAtStartOfDay())) {
				//chain end determined by start time of next anchor
				end = next.getStart();
			} else {
				//chain end determined by end of available time at this day
				end = otService.getLocationAvailableTime(anchor.getLocation(), anchor.getStart()).getEnd();
			}
			int minutes = Minutes.minutesBetween(anchor.getStart(), end).getMinutes();
			anchor.setMaxChainLengthInMinutes(minutes);
		}

		//add null anchor - is workaround because Optaplanner 6.0.1.Final doesn't support chained and nullable planning
		// variables. PlannedSurgeries that are attached to this anchor are considered unscheduled.
		anchors.add(new Anchor(null, time.now()));

		return anchors;
	}

	/**
	 * return already started surgery for this location<br />
	 *
	 * @param surgeries
	 * @param location
	 * @return
	 */
	private Surgery getStartedSurgery(List<Surgery> surgeries, Location location) {
		for (Surgery surgery : surgeries) {
			if (location.equals(surgery.getSchedulingData().getLocation()) && surgery.getDateStarted() != null) {
				return surgery;
			}
		}
		return null;
	}

	/**
	 * creates a Timetable object that represents the solution and calls that appropriate setter methods for each param
	 *
	 * @param anchors
	 * @param plannedSurgeries
	 * @return
	 */
	private Timetable setupTimetable(List<Anchor> anchors, List<PlannedSurgery> plannedSurgeries) {
		Timetable timetable = new Timetable();
		//set planning facts
		timetable.setAnchors(anchors);

		//set planning entity
		timetable.setPlannedSurgeries(plannedSurgeries);
		return timetable;
	}

	/**
	 * This functions creates PlannedSurgery objects and chains them according to the last solution and alterations.
	 *
	 * @param surgeries
	 * @param anchors
	 * @return
	 */
	private List<PlannedSurgery> setupPlannedSurgeryChains(List<Surgery> surgeries, List<Anchor> anchors,
	                                                       DateTime lastPlannedDay) {
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();

		//set location start time and location of last solution
		for (Surgery surgery : surgeries) {
			PlannedSurgery plannedSurgery = new PlannedSurgery(otService);
			plannedSurgery.setSurgery(surgery);
			SchedulingData scheduling = surgery.getSchedulingData();
			if (scheduling != null && scheduling.getStart() != null) {
				plannedSurgery.setStart(scheduling.getStart());
				plannedSurgery.setLocation(scheduling.getLocation());
			}
			//only add surgeries that are unscheduled or within the planning window
			if (plannedSurgery.getStart() == null || plannedSurgery.getStart().isBefore(lastPlannedDay)) {
				plannedSurgeries.add(plannedSurgery);
			}
		}

		//sort anchors and plannedSurgeries by location and start time
		Collections.sort(anchors, new TimetableEntryComparator());
		Collections.sort(plannedSurgeries, new TimetableEntryComparator());

		//build chains
		ListIterator<Anchor> anchorIterator = anchors.listIterator();
		Anchor currentAnchor = anchorIterator.next();
		Anchor nextAnchor = anchorIterator.next();
		Anchor nextAnchorSameLocation = getNextAnchorInSameLocation(currentAnchor, nextAnchor);

		List<PlannedSurgery> chain = new ArrayList<PlannedSurgery>();
		for (PlannedSurgery ps : plannedSurgeries) {
			//this surgery hasn't been scheduled yet and is therefore not part of a chain
			if (ps.getLocation() == null || ps.getStart() == null) {
				continue;
			}

			//plannedSurgery location different than anchor location -> goto next anchor
			while ((currentAnchor.getLocation() == null && ps.getLocation() != null) ||
					!currentAnchor.getLocation().equals(ps.getLocation())) {
				//connect elements
				connectChain(chain, currentAnchor);
				chain.clear();
				//update anchors
				currentAnchor = nextAnchor;
				nextAnchor = anchorIterator.hasNext() ? anchorIterator.next() : null;
				nextAnchorSameLocation = getNextAnchorInSameLocation(currentAnchor, nextAnchor);
			}

			//if there is a subsequent anchor for the same location and it starts before the planned surgery -> goto next anchor
			while (nextAnchorSameLocation != null && !ps.getStart().isBefore(nextAnchorSameLocation.getStart())) {
				//connect elements
				connectChain(chain, currentAnchor);
				chain.clear();
				//update anchors
				currentAnchor = nextAnchor;
				nextAnchor = anchorIterator.hasNext() ? anchorIterator.next() : null;
				nextAnchorSameLocation = getNextAnchorInSameLocation(currentAnchor, nextAnchor);
			}

			chain.add(ps);
		}
		connectChain(chain, currentAnchor);
		return plannedSurgeries;
	}

	/**
	 * converts the dynamic array into linked list
	 *
	 * @param plannedSurgeries
	 * @param anchor
	 */
	private void connectChain(List<PlannedSurgery> plannedSurgeries, Anchor anchor) {
		//create chain with current anchor
		TimetableEntry last = anchor;
		System.out.print("Chain: " + anchor + " -> ");
		for (PlannedSurgery ps : plannedSurgeries) {
			ps.setPreviousTimetableEntry(last);
			System.out.print(ps.getSurgery() + " -> ");
			last = ps;
		}
		System.out.println();
	}

	/**
	 * returns anchor that must have the same location as currentAnchor and must be the next element after currentAnchor
	 *
	 * @param currentAnchor
	 * @param nextAnchor    anchor after currentAnchor
	 * @return
	 */
	private Anchor getNextAnchorInSameLocation(Anchor currentAnchor, Anchor nextAnchor) {
		if (nextAnchor == null) {
			return null;
		}
		if (nextAnchor.getLocation().equals(currentAnchor.getLocation())) {
			return nextAnchor;
		} else {
			return null;
		}
	}

	public static enum Status {
		PRISTINE, RUNNING, SUCCEEDED, FAILED
	}
}
