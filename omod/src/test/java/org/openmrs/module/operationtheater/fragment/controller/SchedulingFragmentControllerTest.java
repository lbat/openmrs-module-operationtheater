package org.openmrs.module.operationtheater.fragment.controller;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.ui.framework.SimpleObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchedulingFragmentController}
 */
public class SchedulingFragmentControllerTest {

	private final String DEFAULT_AVAILABLE_TIME_BEGIN_UUID = "4e051aeb-a19d-49e0-820f-51ae591ec41f";

	private final String DEFAULT_AVAILABLE_TIME_END_UUID = "a9d9ec55-e992-4d04-aebe-808be50aa87a";

	private final String LOCATION_TAG_OPERATION_THEATER_UUID = "af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2";

	private final String APPT_TYPE_UUID = "93263567-286d-4567-8596-0611d9800206";

	/**
	 * @verifies return scheduled surgeries and available times for all operating theaters
	 * @see SchedulingFragmentController#getEvents(org.openmrs.ui.framework.UiUtils, java.util.Date, java.util.Date, java.util.List, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void getEvents_shouldReturnScheduledSurgeriesAndAvailableTimesForAllOperatingTheaters() throws Exception {
		//prepare parameters
		Date start = new DateTime(2014, 6, 9, 0, 0).toDate();
		Date end = new DateTime(2014, 6, 9, 23, 59).toDate();
		List<String> resources = new ArrayList<String>();
		resources.add("ot 1");
		resources.add("ot 2");
		resources.add("ot 3");

		//prepare service layer return objects
		LocationTag tag = new LocationTag();
		List<Location> locations = new ArrayList<Location>();

		LocationAttributeType beginAttributeType = new LocationAttributeType();
		beginAttributeType.setUuid(DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
		LocationAttributeType endAttributeType = new LocationAttributeType();
		endAttributeType.setUuid(DEFAULT_AVAILABLE_TIME_END_UUID);

		LocationAttribute defaultBeginAttr = Mockito.spy(new LocationAttribute());
		defaultBeginAttr.setValue("08:45");
		Mockito.doReturn(beginAttributeType).when(defaultBeginAttr).getDescriptor();
		LocationAttribute defaultEndAttr = Mockito.spy(new LocationAttribute());
		Mockito.doReturn(endAttributeType).when(defaultEndAttr).getDescriptor();
		defaultEndAttr.setUuid(DEFAULT_AVAILABLE_TIME_END_UUID);
		defaultEndAttr.setValue("19:16");
		Set<LocationAttribute> attributes = new HashSet<LocationAttribute>();
		attributes.add(defaultBeginAttr);
		attributes.add(defaultEndAttr);

		Location ot1 = new Location();
		ot1.setId(1);
		ot1.setName("ot 1");
		ot1.setAttributes(attributes);
		locations.add(ot1);

		Location ot2 = new Location();
		ot2.setId(2);
		ot2.setName("ot 2");
		ot2.setAttributes(attributes);
		locations.add(ot2);

		Location ot3 = new Location();
		ot3.setId(3);
		ot3.setName("ot 3");
		ot3.setAttributes(attributes);
		locations.add(ot3);

		List<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();
		AppointmentBlock blockOt2 = new AppointmentBlock();
		blockOt2.setLocation(ot2);
		DateTime blockStartDate = new DateTime(2014, 6, 9, 7, 35);
		blockOt2.setStartDate(blockStartDate.toDate());
		DateTime blockEndDate = new DateTime(2014, 6, 9, 20, 55);
		blockOt2.setEndDate(blockEndDate.toDate());
		blocks.add(blockOt2);

		//ot3 is not available for this day
		AppointmentBlock blockOt3 = new AppointmentBlock();
		blockStartDate = blockStartDate.withTimeAtStartOfDay();
		blockOt3.setLocation(ot3);
		blockOt3.setStartDate(blockStartDate.toDate());
		blockOt3.setEndDate(blockStartDate.toDate());
		blocks.add(blockOt3);

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

		doReturn(tag).when(locationService).getLocationTagByUuid(LOCATION_TAG_OPERATION_THEATER_UUID);
		doReturn(locations).when(locationService).getLocationsByTag(tag);
		doReturn(blocks).when(appointmentService).getAppointmentBlocks(start, end, "1,2,3,", null, null);

		//call function under test
		List<SimpleObject> result = new SchedulingFragmentController().getEvents(new TestUiUtils(),
				start, end, resources, locationService, appointmentService);

		//verify
		for (SimpleObject o : result) {
			System.err.println(o.toJson());
		}

		assertThat(result, hasSize(5));
		assertThat(result.get(0).toJson(),
				is("{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 08:45\",\"availableStart\":\"2014-06-09 08:45\",\"availableEnd\":\"2014-06-09 19:16\",\"resourceId\":1,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\"}"));
		assertThat(result.get(1).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 19:16\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 08:45\",\"availableEnd\":\"2014-06-09 19:16\",\"resourceId\":1,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\"}"));

		assertThat(result.get(2).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 07:35\",\"availableStart\":\"2014-06-09 07:35\",\"availableEnd\":\"2014-06-09 20:55\",\"resourceId\":2,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\"}"));
		assertThat(result.get(3).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 20:55\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 07:35\",\"availableEnd\":\"2014-06-09 20:55\",\"resourceId\":2,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\"}"));

		assertThat(result.get(4).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 00:00\",\"availableEnd\":\"2014-06-09 23:59\",\"resourceId\":3,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\"}"));
	}

	/**
	 * @verifies create appointment block if available times differ from default ones
	 * @see SchedulingFragmentController#adjustAvailableTimes(org.openmrs.ui.framework.UiUtils, String, boolean, java.util.Date, java.util.Date, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void adjustAvailableTimes_shouldCreateAppointmentBlockIfAvailableTimesDifferFromDefaultOnes() throws Exception {
		String uuid = "1";
		Location location = new Location();
		location.setUuid(uuid);

		Date start = new DateTime(2014, 6, 9, 7, 12).toDate();
		Date end = new DateTime(2014, 6, 9, 20, 55).toDate();

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

		doReturn(location).when(locationService).getLocationByUuid(uuid);
		doReturn(null).when(appointmentService)
				.getAppointmentBlocks(Matchers.any(Date.class), Matchers.any(Date.class), eq("1"),
						Matchers.any(Provider.class), Matchers.any(AppointmentType.class));

		ArgumentCaptor<AppointmentBlock> argumentCaptor = ArgumentCaptor.forClass(AppointmentBlock.class);
		when(appointmentService.saveAppointmentBlock(argumentCaptor.capture())).thenReturn(null);

		//call function under test
		new SchedulingFragmentController()
				.adjustAvailableTimes(new TestUiUtils(), uuid, true, start, end, locationService, appointmentService);

		assertThat(argumentCaptor.getValue().getLocation(), equalTo(location));
		assertThat(argumentCaptor.getValue().getStartDate(), equalTo(start));
		assertThat(argumentCaptor.getValue().getEndDate(), equalTo(end));

	}

	/**
	 * @verifies update appointment block if entry already exists
	 * @see SchedulingFragmentController#adjustAvailableTimes(org.openmrs.ui.framework.UiUtils, String, boolean, java.util.Date, java.util.Date, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void adjustAvailableTimes_shouldUpdateAppointmentBlockIfEntryAlreadyExists() throws Exception {
		String uuid = "afd1b8c2-eac7-4db4-b689-911615cebf80";
		Location location = new Location();
		location.setUuid(uuid);
		location.setId(10);

		List<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();
		AppointmentBlock block = new AppointmentBlock();
		block.setUuid("2");
		blocks.add(block);

		Date start = new DateTime(2014, 6, 9, 7, 12).toDate();
		Date end = new DateTime(2014, 6, 9, 20, 55).toDate();

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

		doReturn(location).when(locationService).getLocationByUuid(uuid);
		doReturn(blocks).when(appointmentService)
				.getAppointmentBlocks(Matchers.any(Date.class), Matchers.any(Date.class), eq(uuid),
						Matchers.any(Provider.class), Matchers.any(AppointmentType.class));

		ArgumentCaptor<AppointmentBlock> argumentCaptor = ArgumentCaptor.forClass(AppointmentBlock.class);
		when(appointmentService.saveAppointmentBlock(argumentCaptor.capture())).thenReturn(null);

		//call function under test
		new SchedulingFragmentController()
				.adjustAvailableTimes(new TestUiUtils(), uuid, true, start, end, locationService, appointmentService);

		assertThat(argumentCaptor.getValue().getId(), is(block.getId()));
		assertThat(argumentCaptor.getValue().getLocation(), equalTo(location));
		assertThat(argumentCaptor.getValue().getStartDate(), equalTo(start));
		assertThat(argumentCaptor.getValue().getEndDate(), equalTo(end));
	}
}
