package org.openmrs.module.operationtheater.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.Scheduler;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.validator.ValidateUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SchedulingFragmentController {

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Context.getLocale());

	protected Log log = LogFactory.getLog(getClass());

	/**
	 * @param ui
	 * @param start
	 * @param end
	 * @param resources
	 * @param locationService
	 * @param appointmentService
	 * @return
	 * @should return scheduled surgeries and available times for all operating theaters
	 */
	public List<SimpleObject> getEvents(UiUtils ui,
	                                    @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
	                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end,
	                                    @RequestParam("resources") List<String> resources,
	                                    @SpringBean("locationService") LocationService locationService,
	                                    @SpringBean AppointmentService appointmentService,
	                                    @SpringBean OperationTheaterService otService) {

		//get operation theaters
		LocationTag tag = locationService.getLocationTagByUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		List<Location> locations = locationService.getLocationsByTag(tag);

		//build string "locationID1, locationID2, ..."
		String locationsString = buildLocationString(locations);

		//get associated appointmentBlocks
		List<AppointmentBlock> blocks = appointmentService.getAppointmentBlocks(start, end, locationsString, null, null);

		//build an index to find appointmentBlock with location and startDate
		Map<Location, Map<DateTime, AppointmentBlock>> indexedApptBlocks = indexApptBlocks(locations, blocks);

		//iterate over all location and dates and add events for the calendar
		List<CalendarEvent> events = new ArrayList<CalendarEvent>();
		DateTime endDate = new DateTime(end);
		for (Location location : locations) {
			for (DateTime startDate = new DateTime(start); startDate.isBefore(endDate); startDate = startDate.plusDays(1)) {
				addAvailableTimesToEventList(events, startDate, indexedApptBlocks, location, resources);
			}
		}

		//add surgeries
		List<Surgery> surgeryList = otService.getScheduledSurgeries(new DateTime(start), endDate);
		for (Surgery surgery : surgeryList) {
			CalendarEvent event = getSurgeryCalendarEvent(resources, surgery);
			events.add(event);
		}

		return SimpleObject
				.fromCollection(events, ui, "title", "start", "end", "availableStart", "availableEnd", "surgeryUuid",
						"patientUuid",
						"dateLocked", "resourceId", "allDay", "editable", "annotation", "color");
	}

	/**
	 * @should return SuccessResult if solve method doesnt throw an IllegalStateException
	 * @should return FailureResult if solve method throws an IllegalStateException
	 */
	public FragmentActionResult schedule(UiUtils ui) {
		try {
			Scheduler.INSTANCE.solve();
			return new SuccessResult(ui.message("operationtheater.scheduling.startedSuccessfully"));
		}
		catch (IllegalStateException e) {
			return new FailureResult(ui.message("operationtheater.scheduling.schedulerAlreadyRunning"));
		}

	}

	/**
	 * @param ui
	 * @return
	 * @should return SuccessResult if status is running or succeeded
	 * @should return FailureResult if status is failed, pristine or any other
	 */
	public FragmentActionResult getSolverStatus(UiUtils ui) {
		switch (Scheduler.INSTANCE.getStatus()) {
			case RUNNING:
				return new SuccessResult("running");
			case SUCCEEDED:
				return new SuccessResult(ui.message("operationtheater.scheduling.finishedSuccessfully"));
			case FAILED:
				return new FailureResult(ui.message("operationtheater.scheduling.schedulingFailed"));
			case PRISTINE:
				return new FailureResult(ui.message("operationtheater.scheduling.schedulerNotStarted"));
		}
		return new FailureResult("uncaughtException.title");
	}

	/**
	 * @param surgeryUuid
	 * @param locationUuid
	 * @param scheduledDateTime
	 * @param lockedDate
	 * @param locationService
	 * @param otService
	 *
	 * @should update SchedulingData with provided values and return SuccessResult
	 * @should throw IllegalArgumentException if there is no Surgery for the given uuid
	 * @should return FailureResult if SchedulingData validation fails
	 */
	public FragmentActionResult adjustSurgerySchedule(UiUtils ui,
	                                                  @RequestParam("surgeryUuid") String surgeryUuid,
	                                                  @RequestParam("scheduledLocationUuid") String locationUuid,
	                                                  @RequestParam("start") @DateTimeFormat(
			                                                  iso = DateTimeFormat.ISO.DATE_TIME) Date scheduledDateTime,
	                                                  @RequestParam("lockedDate") boolean lockedDate,
	                                                  @SpringBean("locationService") LocationService locationService,
	                                                  @SpringBean OperationTheaterService otService) {

		Location location = locationService.getLocationByUuid(
				locationUuid); //TODO replace with @ModelAttribute("location") @BindParams(value="scheduledLocationUuid") Location location

		Surgery surgery = otService.getSurgeryByUuid(surgeryUuid);
		if (surgery == null) {
			throw new IllegalArgumentException("No surgery entry found for surgeryUuid: " + surgeryUuid);
		}

		DateTime start = new DateTime(scheduledDateTime);
		Procedure p = surgery.getProcedure();
		int duration = p.getInterventionDuration() + p.getOtPreparationDuration();
		DateTime end = start.plusMinutes(duration);

		SchedulingData schedulingData = surgery.getSchedulingData();
		schedulingData.setDateLocked(lockedDate);
		schedulingData.setStart(start);
		schedulingData.setEnd(end);
		schedulingData.setLocation(location);

		MapBindingResult errors = new MapBindingResult(new HashMap<String, String>(), SchedulingData.class.getName());
		ValidateUtil.validate(schedulingData, errors);
		if (errors.hasErrors()) {
			return new FailureResult(errors);
		}

		//persist
		otService.saveSurgery(surgery);

		return new SuccessResult(ui.message("operationtheater.scheduling.page.surgeryAdjustedSuccessfully"));
	}

	/**
	 * @param ui
	 * @param uuid
	 * @param available
	 * @param start
	 * @param end
	 * @param locationService
	 * @param appointmentService
	 * @throws Exception
	 * @should create appointment block if available times differ from default ones
	 * @should update appointment block if entry already exists
	 */
	public void adjustAvailableTimes(UiUtils ui,
	                                 @RequestParam("locationUuid") String uuid,
	                                 @RequestParam("available") boolean available,
	                                 @RequestParam("startTime") @DateTimeFormat(
			                                 iso = DateTimeFormat.ISO.DATE_TIME) Date start,
	                                 @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end,
	                                 @SpringBean("locationService") LocationService locationService,
	                                 @SpringBean AppointmentService appointmentService) throws Exception {

		if (available && new DateTime(start).isAfter(new DateTime(end))) {
			throw new IllegalArgumentException("start date must be before end date"); //TODO send meaningful error message
		}

		//TODO sanitize uuid - sql injection
		Location location = locationService.getLocationByUuid(uuid);
		if (location == null) {
			throw new IllegalArgumentException("no location found for uuid: " + uuid); //TODO send meaningful error message
		}

		DateTime startOfDay = new DateTime(start).withTimeAtStartOfDay();

		AppointmentBlock block = getOrCreateAppointmentBlock(appointmentService, location, startOfDay);

		if (!available) {
			block.setStartDate(startOfDay.toDate());
			block.setEndDate(startOfDay.toDate());
		} else {
			block.setStartDate(start);
			block.setEndDate(end);
		}
		appointmentService.saveAppointmentBlock(block);
	}

	//-------------------------------------------------------------------------------------
	// PRIVATE METHODS
	//-------------------------------------------------------------------------------------

	/**
	 * creates new surgery calendar event
	 *
	 * @param resources
	 * @param surgery
	 * @return
	 */
	private CalendarEvent getSurgeryCalendarEvent(List<String> resources, Surgery surgery) {
		SchedulingData scheduling = surgery.getSchedulingData();
		if (scheduling == null || scheduling.getStart() == null || scheduling.getEnd() == null
				|| scheduling.getLocation() == null) {
			return null;
		}
		String startStr = dateFormatter.format(scheduling.getStart().toDate());
		String endStr = dateFormatter.format(scheduling.getEnd().toDate());
		String patientName = surgery.getPatient().getFamilyName() + " " + surgery.getPatient().getGivenName();
		String procedure = surgery.getProcedure().getName();
		String surgeryUuid = surgery.getUuid();
		String patientUuid = surgery.getPatient().getUuid();
		String color = getCalendarColor(scheduling.getLocation());
		//convention: resourceId = element array index + 1 - TODO thats a bit hacky -> refactor that
		int resourceId = resources.indexOf(scheduling.getLocation().getName()) + 1;
		CalendarEvent event = new CalendarEvent(procedure + " - " + patientName, startStr, endStr, surgeryUuid, patientUuid,
				scheduling.getDateLocked(), resourceId, color);
		return event;
	}

	/**
	 * returns calendar color value if this location attribute is specified
	 *
	 * @param location
	 * @return
	 */
	private String getCalendarColor(Location location) {
		LocationAttribute colorAttr = getAttributeByUuid(location.getActiveAttributes(),
				OTMetadata.CALENDAR_COLOR_UUID);
		String color = null;
		if (colorAttr != null) {
			color = (String) colorAttr.getValue();
		}
		return color;
	}

	/**
	 * returns an existing AppointmentBlock or creates a new one
	 *
	 * @param appointmentService
	 * @param location
	 * @param midnight
	 * @return
	 * @throws Exception
	 */
	private AppointmentBlock getOrCreateAppointmentBlock(AppointmentService appointmentService, Location location,
	                                                     DateTime midnight) throws Exception {
		AppointmentBlock block = new AppointmentBlock();
		List<AppointmentBlock> blocks = appointmentService
				.getAppointmentBlocks(midnight.toDate(), midnight.plusDays(1).minusSeconds(1).toDate(),
						String.valueOf(location.getId()), null, null);
		if (blocks.size() > 1) {
			throw new IllegalStateException("There should only be one AppointmentBlock per Location and day, but returned " + blocks
					.size());
		}
		if (blocks.size() == 1) {
			block = blocks.get(0);
		} else {
			block.setLocation(location);
			//TODO what is the appointment type for? - resolve that - just added a random one
			AppointmentType type = appointmentService.getAppointmentTypeByUuid(OTMetadata.APPT_TYPE_UUID);
			HashSet<AppointmentType> set = new HashSet<AppointmentType>();
			set.add(type);
			block.setTypes(set);
		}
		return block;
	}

	/**
	 * creates available time events for the given location
	 *
	 * @param events
	 * @param startDate
	 * @param indexedApptBlocks
	 * @param location
	 * @param resources
	 */
	private void addAvailableTimesToEventList(List<CalendarEvent> events, DateTime startDate,
	                                          Map<Location, Map<DateTime, AppointmentBlock>> indexedApptBlocks,
	                                          Location location, List<String> resources) {

		AppointmentBlock block = indexedApptBlocks.get(location).get(startDate.withTimeAtStartOfDay());
		//convention: resourceId = element array index + 1 - TODO thats a bit hacky -> refactor that
		int resourceId = resources.indexOf(location.getName()) + 1;

		String start = "";
		String end = "";
		if (block != null) {
			start = dateFormatter.format(block.getStartDate());
			end = dateFormatter.format(block.getEndDate());
		} else {
			//no appointmentBlock found -> use default value (LocationAttribute)
			LocationAttribute defaultBegin = getAttributeByUuid(location.getActiveAttributes(),
					OTMetadata.DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
			LocationAttribute defaultEnd = getAttributeByUuid(location.getActiveAttributes(),
					OTMetadata.DEFAULT_AVAILABLE_TIME_END_UUID);

			if (defaultBegin == null || defaultEnd == null) {
				return;
			}

			DateTimeFormatter timeFormatter = OTMetadata.AVAILABLE_TIME_FORMATTER;
			LocalTime beginTime = LocalTime.parse((String) defaultBegin.getValue(), timeFormatter);
			LocalTime endTime = LocalTime.parse((String) defaultEnd.getValue(), timeFormatter);

			start = dateFormatter
					.format(startDate.withTime(beginTime.getHourOfDay(), beginTime.getMinuteOfHour(), 0, 0).toDate());
			end = dateFormatter.format(startDate.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(), 0, 0).toDate());
		}

		String beginOfDay = dateFormatter.format(startDate.toDate());
		String endOfDay = dateFormatter.format(startDate.plusHours(24).minusMinutes(1).toDate());
		if (start.equals(end)) {
			//location is not available for this day
			CalendarEvent event = new CalendarEvent("", beginOfDay, endOfDay, beginOfDay, beginOfDay, resourceId, true);
			events.add(event);
		} else {
			CalendarEvent morning = new CalendarEvent("", beginOfDay, start, start, end, resourceId, true);
			events.add(morning);
			CalendarEvent evening = new CalendarEvent("", end, endOfDay, start, end, resourceId, true);
			events.add(evening);
		}
	}

	/**
	 * helper function to return LocationAttribute for a given uuid
	 *
	 * @param attributes
	 * @param uuid
	 * @return
	 */
	private LocationAttribute getAttributeByUuid(Collection<LocationAttribute> attributes, String uuid) {
		for (LocationAttribute attribute : attributes) {
			if (attribute.getDescriptor().getUuid().equals(uuid)) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * helper function that concatenates all location ids seperated with comma
	 *
	 * @param locations
	 * @return
	 */
	private String buildLocationString(List<Location> locations) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Location location : locations) {
			stringBuilder.append(location.getLocationId()).append(",");
		}
		return stringBuilder.toString();
	}

	/**
	 * helper function that creates a double lookup map to for convenient retrieval of the right AppointmentBlock
	 * given location and date
	 *
	 * @param locations
	 * @param appointmentBlocks
	 * @return
	 */
	private Map<Location, Map<DateTime, AppointmentBlock>> indexApptBlocks(List<Location> locations,
	                                                                       List<AppointmentBlock> appointmentBlocks) {
		Map<Location, Map<DateTime, AppointmentBlock>> map = new HashMap<Location, Map<DateTime, AppointmentBlock>>();
		for (Location location : locations) {
			map.put(location, new HashMap<DateTime, AppointmentBlock>());
		}
		for (AppointmentBlock appointmentBlock : appointmentBlocks) {
			DateTime dateTime = new DateTime(appointmentBlock.getStartDate()).withTimeAtStartOfDay();
			map.get(appointmentBlock.getLocation()).put(dateTime, appointmentBlock);
		}
		return map;
	}

	public static class CalendarEvent {

		/**
		 * Title of this event that is displayed in the calendar
		 * empty if it is an available time entry
		 */
		private String title = "";

		/**
		 * start time of this event
		 */
		private String start;

		/**
		 * end time of this event
		 */
		private String end;

		/**
		 *
		 */
		private String availableStart = "";

		private String availableEnd = "";

		private int resourceId;

		private boolean allDay = false;

		private boolean editable = false;

		/**
		 * if event is related to available time this flag is true
		 */
		private boolean annotation;

		private String color = "blue";

		private String surgeryUuid;

		private String patientUuid;

		private boolean dateLocked;

		public CalendarEvent(String title, String start, String end, String availableStart, String availableEnd,
		                     int resourceId,
		                     boolean annotation) {
			this.title = title;
			this.start = start;
			this.end = end;
			this.availableStart = availableStart;
			this.availableEnd = availableEnd;
			this.resourceId = resourceId;
			this.annotation = annotation;
			if (annotation) {
				color = "grey";
			}
		}

		public CalendarEvent(String title, String start, String end, String surgeryUuid, String patientUuid,
		                     boolean dateLocked,
		                     int resourceId, String color) {
			this.title = title;
			this.start = start;
			this.end = end;
			this.surgeryUuid = surgeryUuid;
			this.dateLocked = dateLocked;
			this.resourceId = resourceId;
			annotation = false;
			if (color != null) {
				this.color = color;
			}
			this.patientUuid = patientUuid;
		}

		public String getAvailableStart() {
			return availableStart;
		}

		public void setAvailableStart(String availableStart) {
			this.availableStart = availableStart;
		}

		public String getAvailableEnd() {
			return availableEnd;
		}

		public void setAvailableEnd(String availableEnd) {
			this.availableEnd = availableEnd;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public boolean getAnnotation() {
			return annotation;
		}

		public void setAnnotation(boolean annotation) {
			this.annotation = annotation;
			if (annotation) {
				color = "grey";
			} else {
				color = "blue";
			}

		}

		public boolean getAllDay() {
			return allDay;
		}

		public void setAllDay(boolean allDay) {
			this.allDay = allDay;
		}

		public boolean getEditable() {
			return editable;
		}

		public void setEditable(boolean editable) {
			this.editable = editable;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getStart() {
			return start;
		}

		public void setStart(String start) {
			this.start = start;
		}

		public String getEnd() {
			return end;
		}

		public void setEnd(String end) {
			this.end = end;
		}

		public int getResourceId() {
			return resourceId;
		}

		public void setResourceId(int resourceId) {
			this.resourceId = resourceId;
		}

		public String getSurgeryUuid() {
			return surgeryUuid;
		}

		public void setSurgeryUuid(String surgeryUuid) {
			this.surgeryUuid = surgeryUuid;
		}

		public String getPatientUuid() {
			return patientUuid;
		}

		public void setPatientUuid(String patientUuid) {
			this.patientUuid = patientUuid;
		}

		public boolean isDateLocked() {
			return dateLocked;
		}

		public void setDateLocked(boolean dateLocked) {
			this.dateLocked = dateLocked;
		}

		@Override
		public String toString() {
			return "CalendarEvent{" +
					"title='" + title + '\'' +
					", start='" + start + '\'' +
					", end='" + end + '\'' +
					", availableStart='" + availableStart + '\'' +
					", availableEnd='" + availableEnd + '\'' +
					", resourceId=" + resourceId +
					", allDay=" + allDay +
					", editable=" + editable +
					", annotation=" + annotation +
					", color='" + color + '\'' +
					'}';
		}
	}
}
