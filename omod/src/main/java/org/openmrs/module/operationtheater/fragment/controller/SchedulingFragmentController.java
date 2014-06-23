package org.openmrs.module.operationtheater.fragment.controller;

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
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.format.annotation.DateTimeFormat;
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

	private final String DEFAULT_AVAILABLE_TIME_BEGIN_UUID = "4e051aeb-a19d-49e0-820f-51ae591ec41f";

	private final String DEFAULT_AVAILABLE_TIME_END_UUID = "a9d9ec55-e992-4d04-aebe-808be50aa87a";

	private final String LOCATION_TAG_OPERATION_THEATER_UUID = "af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2";

	private final String APPT_TYPE_UUID = "93263567-286d-4567-8596-0611d9800206";

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
	                                    @SpringBean AppointmentService appointmentService) {

		//get operation theaters
		LocationTag tag = locationService.getLocationTagByUuid(LOCATION_TAG_OPERATION_THEATER_UUID);
		List<Location> locations = locationService.getLocationsByTag(tag);

		//build string "locationID1, locationID2, ..."
		String locationsString = buildLocationString(locations);
		System.err.println("location string: " + locationsString);

		//get associated appointmentBlocks
		List<AppointmentBlock> blocks = appointmentService.getAppointmentBlocks(start, end, locationsString, null, null);
		System.err.println("# of Appt Blocks: " + blocks.size());

		//build an index to find appointmentBlock with location and startDate
		Map<Location, Map<DateTime, AppointmentBlock>> indexedApptBlocks = indexApptBlocks(locations, blocks);

		//iterate over all location and dates and add events for the calendar
		List<CalendarEvent> events = new ArrayList<CalendarEvent>();
		DateTime endDate = new DateTime(end);
		for (Location location : locations) {
			for (DateTime startDate = new DateTime(start); startDate.isBefore(endDate); startDate = startDate.plusDays(1)) {
				AppointmentBlock block = indexedApptBlocks.get(location).get(startDate.withTimeAtStartOfDay());
				int resourceId =
						resources.indexOf(location.getName()) + 1; //convention: resourceId = element array index + 1
				System.err.printf("%s - %s - %s - %d", startDate.toDate(), block, location.getName(), resourceId);
				addAvailableTimesToEventList(events, startDate, block, location, resourceId);
			}
		}

		return SimpleObject
				.fromCollection(events, ui, "title", "start", "end", "availableStart", "availableEnd", "resourceId",
						"allDay", "editable", "annotation", "color");
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

		if (new DateTime(start).isAfter(new DateTime(end))) {
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

	private AppointmentBlock getOrCreateAppointmentBlock(AppointmentService appointmentService, Location location,
	                                                     DateTime midnight) throws Exception {
		AppointmentBlock block = new AppointmentBlock();
		List<AppointmentBlock> blocks = appointmentService
				.getAppointmentBlocks(midnight.toDate(), midnight.plusDays(1).minusSeconds(1).toDate(),
						String.valueOf(location.getId()), null, null);
		if (blocks.size() > 1) {
			for (AppointmentBlock block1 : blocks) {
				System.err.println(block1);
			}
			throw new Exception("there should only be one AppointmentBlock per Location and day, but returned " + blocks
					.size()); //TODO find better exception
		}
		if (blocks.size() == 1) {
			block = blocks.get(0);
		} else {
			block.setLocation(location);
			AppointmentType type = appointmentService.getAppointmentTypeByUuid(
					APPT_TYPE_UUID); //TODO what is the appointment type for? - resolve that - just added a random one
			HashSet<AppointmentType> set = new HashSet<AppointmentType>();
			set.add(type);
			block.setTypes(set);
		}
		return block;
	}

	private void addAvailableTimesToEventList(List<CalendarEvent> events, DateTime startDate,
	                                          AppointmentBlock appointmentBlock, Location location, int resourceId) {
		String beginOfDay = dateFormatter.format(startDate.toDate());
		String endOfDay = dateFormatter.format(startDate.plusHours(24).minusMinutes(1).toDate());
		String start = "";
		String end = "";
		if (appointmentBlock != null) {
			start = dateFormatter.format(appointmentBlock.getStartDate());
			end = dateFormatter.format(appointmentBlock.getEndDate());

		} else {
			//no appointmentBlock found -> use default value (LocationAttribute)
			LocationAttribute defaultBegin = getAttributeByUuid(location.getActiveAttributes(),
					DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
			LocationAttribute defaultEnd = getAttributeByUuid(location.getActiveAttributes(),
					DEFAULT_AVAILABLE_TIME_END_UUID);

			if (defaultBegin == null || defaultEnd == null) {
				return;
			}

			DateTimeFormatter timeFormatter = org.joda.time.format.DateTimeFormat.forPattern("HH:mm");
			LocalTime beginTime = LocalTime.parse((String) defaultBegin.getValue(), timeFormatter);
			LocalTime endTime = LocalTime.parse((String) defaultEnd.getValue(), timeFormatter);

			startDate = startDate.withTime(beginTime.getHourOfDay(), beginTime.getMinuteOfHour(), 0, 0);
			DateTime endDate = startDate.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(), 0, 0);

			start = dateFormatter.format(startDate.toDate());
			end = dateFormatter.format(endDate.toDate());
		}

		if (start.equals(end)) {
			//location is not available for this day
			CalendarEvent event = new CalendarEvent("", beginOfDay, endOfDay, beginOfDay, endOfDay, resourceId, true);
			events.add(event);
		} else {
			CalendarEvent morning = new CalendarEvent("", beginOfDay, start, start, end, resourceId, true);
			events.add(morning);
			CalendarEvent evening = new CalendarEvent("", end, endOfDay, start, end, resourceId, true);
			events.add(evening);
		}

		//TODO remove: just for demo purpose
		if(location.getName().equals("OT 1") && startDate.getDayOfMonth()==23) {
			startDate = startDate.withTime(0, 0, 0, 0);
			start = dateFormatter.format(startDate.plusHours(13).toDate());
			end = dateFormatter.format(startDate.plusHours(14).toDate());
			CalendarEvent surgery = new CalendarEvent("Appendectomy - Carter, Partricia", start, end, resourceId);
			events.add(surgery);
		}
	}

	private LocationAttribute getAttributeByUuid(Collection<LocationAttribute> attributes, String uuid) {
		for (LocationAttribute attribute : attributes) {
			if (attribute.getDescriptor().getUuid().equals(uuid)) {
				return attribute;
			}
		}
		return null;
	}

	private String buildLocationString(List<Location> locations) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Location location : locations) {
			stringBuilder.append(location.getLocationId()).append(",");
		}
		return stringBuilder.toString();
	}

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

		private String color = "blue"; //TODO define color as LocationAttribute

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

		public CalendarEvent(String title, String start, String end, int resourceId) {
			this.title = title;
			this.start = start;
			this.end = end;
			this.resourceId = resourceId;
			annotation = false;
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
	}
}
