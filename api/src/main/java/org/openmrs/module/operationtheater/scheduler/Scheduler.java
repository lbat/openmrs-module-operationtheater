package org.openmrs.module.operationtheater.scheduler;

import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
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

public class Scheduler {

	private final String LOCATION_TAG_OPERATION_THEATER_UUID = "af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2";

	private OperationTheaterService otService = Context.getService(OperationTheaterService.class);

	private LocationService locationService = Context.getLocationService();

	/**
	 * @should solve
	 */
	public void solve() {
		//		Class klass = org.slf4j.LoggerFactory.class;
		//		URL location = klass.getResource('/'+klass.getName().replace('.', '/')+".class");
		//		System.err.println(location);
		//
		//		klass = org.slf4j.impl.StaticLoggerBinder.class;
		//		location = klass.getResource('/'+klass.getName().replace('.', '/')+".class");
		//		System.err.println(location);

		// Build the Solver
		SolverFactory solverFactory = new XmlSolverFactory("/scheduler/solverConfig.xml");
		Solver solver = solverFactory.buildSolver();

		// Load problem
		Timetable unsolvedTimetable = createTimetable();

		// Solve the problem
		solver.setPlanningProblem(unsolvedTimetable);
		solver.solve();
		Timetable solvedTimetable = (Timetable) solver.getBestSolution();

		//set surgeries planned begin and finished attributes
		solvedTimetable.persistSolution(otService);

		// Display the result
		System.out.println("\nsolved timetable\n"
				+ solvedTimetable);
	}

	Timetable createTimetable() {
		Timetable timetable = new Timetable();

		//get all uncompleted surgeries
		List<Surgery> surgeries = otService.getAllUncompletedSurgeries();

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

	public void setOperationTheaterService(OperationTheaterService operationTheaterService) {
		otService = operationTheaterService;
	}

	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
}
