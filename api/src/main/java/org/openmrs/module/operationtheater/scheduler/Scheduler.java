package org.openmrs.module.operationtheater.scheduler;

import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.Timetable;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.XmlSolverFactory;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {

	private final String LOCATION_TAG_OPERATION_THEATER_UUID = "af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2";

	private OperationTheaterService otService = Context.getService(OperationTheaterService.class);

	private LocationService locationService = Context.getLocationService();

	/**
	 * @should solve
	 */
	public void solve() {
		// Build the Solver
		SolverFactory solverFactory = new XmlSolverFactory("/scheduler/solverConfig.xml");
		Solver solver = solverFactory.buildSolver();

		// Load problem
		Timetable unsolvedTimetable = createTimetable();

		// Solve the problem
		solver.setPlanningProblem(unsolvedTimetable);
		solver.solve();
		Timetable solvedTimetable = (Timetable) solver.getBestSolution();

		// Display the result
		System.out.println("\nsolved timetable\n"
				+ solvedTimetable);
	}

	private Timetable createTimetable() {
		Timetable timetable = new Timetable();

		//get all surgeries
		List<Surgery> surgeries = otService
				.getAllSurgeries(false); //FIXME change to method that only returns surgeries that don't lie in the past

		//get operation theaters
		LocationTag tag = locationService.getLocationTagByUuid(LOCATION_TAG_OPERATION_THEATER_UUID);
		List<Location> locations = locationService.getLocationsByTag(tag);
		for (Location location : locations) {
			System.out.println(location.getName());
		}

		//generate start dates
		DateTime timestamp = new DateTime();
		DateTime end = timestamp.plusDays(1);
		List<DateTime> startTimes = new ArrayList<DateTime>();
		for (; timestamp.isBefore(end); timestamp = timestamp.plusMinutes(60)) {
			//			System.out.println(timestamp);
			startTimes.add(timestamp);
		}

		//set up initial solution
		List<PlannedSurgery> plannedSurgeries = new ArrayList<PlannedSurgery>();
		for (Surgery surgery : surgeries) {
			PlannedSurgery plannedSurgery = new PlannedSurgery();
			plannedSurgery.setSurgery(surgery);
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

	public void setOperationTheaterService(OperationTheaterService operationTheaterService) {
		otService = operationTheaterService;
	}

	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
}
