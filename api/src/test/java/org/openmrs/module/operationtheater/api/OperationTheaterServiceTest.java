/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.operationtheater.api;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateProcedureDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateSurgeryDAO;
import org.openmrs.module.operationtheater.api.impl.OperationTheaterServiceImpl;
import org.openmrs.validator.ValidateUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ${OperationTheaterService}}.
 * for context sensitive tests have a look at {@link org.openmrs.module.operationtheater.api.OperationTheaterServiceTest1}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ValidateUtil.class)
public class OperationTheaterServiceTest { //extends BaseModuleContextSensitiveTest {

	private OperationTheaterService service;

	private SurgeryDAO surgeryDAO;

	private ProcedureDAO procedureDAO;

	@Before
	public void setUp() {
		service = new OperationTheaterServiceImpl();
		surgeryDAO = Mockito.mock(HibernateSurgeryDAO.class);
		service.setSurgeryDAO(surgeryDAO);
		procedureDAO = Mockito.mock(HibernateProcedureDAO.class);
		service.setProcedureDAO(procedureDAO);
	}

	/**
	 * @verifies validate surgery object and call surgeryDao saveOrUpdate
	 * @see OperationTheaterService#saveSurgery(org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void saveSurgery_shouldValidateSurgeryObject() throws Exception {
		PowerMockito.spy(ValidateUtil.class);

		//do not execute the validate method
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		PowerMockito.doNothing().when(
				ValidateUtil.class, "validate", captor.capture());

		Surgery surgery = new Surgery();
		service.saveSurgery(surgery);

		verify(surgeryDAO).saveOrUpdate(surgery);
		assertThat(captor.getAllValues(), hasSize(1));
		assertEquals(surgery, captor.getValue());
	}

	/**
	 * @verifies validate procedure object and call procedureDao saveOrUpdate
	 * @see OperationTheaterService#saveProcedure(org.openmrs.module.operationtheater.Procedure)
	 */
	@Test
	public void saveProcedure_shouldCreateNewDbEntryIfObjectIsNotNull() throws Exception {
		PowerMockito.spy(ValidateUtil.class);

		//do not execute the validate method
		ArgumentCaptor<Procedure> captor = ArgumentCaptor.forClass(Procedure.class);
		PowerMockito.doNothing().when(
				ValidateUtil.class, "validate", captor.capture());

		Procedure procedure = new Procedure();
		service.saveProcedure(procedure);

		verify(procedureDAO).saveOrUpdate(procedure);
		assertThat(captor.getAllValues(), hasSize(1));
		assertEquals(procedure, captor.getValue());
	}

	/**
	 * @verifies return result of surgeryDAO getAllData method with parameter includeVoided
	 * @see OperationTheaterService#getAllSurgeries(boolean)
	 */
	@Test
	public void getAllSurgeries_shouldReturnResultOfSurgeryDAOGetAllDataMethodWithParameterIncludeVoided() throws Exception {
		List<Surgery> surgeryList = new ArrayList<Surgery>();
		surgeryList.add(new Surgery());

		when(surgeryDAO.getAllData(true)).thenReturn(surgeryList);
		List<Surgery> result = service.getAllSurgeries(true);
		assertEquals(surgeryList, result);
		verify(surgeryDAO).getAllData(true);

		when(surgeryDAO.getAllData(false)).thenReturn(surgeryList);
		result = service.getAllSurgeries(false);
		assertEquals(surgeryList, result);
		verify(surgeryDAO).getAllData(false);
	}

	/**
	 * @verifies call surgeryDAO getByUuid
	 * @see OperationTheaterService#getSurgeryByUuid(String)
	 */
	@Test
	public void getSurgeryByUuid_shouldCallSurgeryDAOGetByUuid() throws Exception {
		String uuid = "ca352fc1-1691-11df-97a5-7038c432aab5";
		Surgery surgery = new Surgery();
		when(surgeryDAO.getByUuid(uuid)).thenReturn(surgery);

		Surgery actualSurgery = service.getSurgeryByUuid(uuid);

		assertEquals(surgery, actualSurgery);
	}

	/**
	 * @verifies call procedureDAO getByUuid
	 * @see OperationTheaterService#getProcedureByUuid(String)
	 */
	@Test
	public void getProcedureByUuid_shouldCallProcedureDAOGetByUuid() throws Exception {
		String uuid = "random uuid";
		Procedure procedure = new Procedure();
		when(procedureDAO.getByUuid(uuid)).thenReturn(procedure);

		Procedure actualProcedure = service.getProcedureByUuid(uuid);

		assertEquals(procedure, actualProcedure);
	}

	/**
	 * @verifies return result of procedureDAO getAll method with parameter includeRetired
	 * @see OperationTheaterService#getAllProcedures(boolean)
	 */
	@Test
	public void getAllProcedures_shouldReturnResultOfProcedureDAOGetAllMethodWithParameterIncludeRetired() throws Exception {
		List<Procedure> procedureList = new ArrayList<Procedure>();
		procedureList.add(new Procedure());

		when(procedureDAO.getAll(true)).thenReturn(procedureList);
		List<Procedure> result = service.getAllProcedures(true);
		assertEquals(procedureList, result);
		verify(procedureDAO).getAll(true);

		when(procedureDAO.getAll(false)).thenReturn(procedureList);
		result = service.getAllProcedures(false);
		assertEquals(procedureList, result);
		verify(procedureDAO).getAll(false);
	}

	/**
	 * @verifies call procedureDAO getById
	 * @see OperationTheaterService#getProcedure(Integer)
	 */
	@Test
	public void getProcedure_shouldCallProcedureDAOGetById() throws Exception {

		Integer id = 1;
		Procedure procedure = new Procedure();

		when(procedureDAO.getById(id)).thenReturn(procedure);

		Procedure result = service.getProcedure(id);

		assertEquals(procedure, result);
	}

	/**
	 * @verifies call surgeryDAO getSurgeriesByPatient
	 * @see OperationTheaterService#getSurgeriesByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getSurgeriesByPatient_shouldCallSurgeryDAOGetSurgeriesByPatient() throws Exception {

		Patient patient = new Patient();

		List<Surgery> expected = new ArrayList<Surgery>();
		when(surgeryDAO.getSurgeriesByPatient(patient)).thenReturn(expected);

		List<Surgery> result = service.getSurgeriesByPatient(patient);

		assertThat(result, is(expected));
	}

	/**
	 * @verifies do nothing if patient is null
	 * @see OperationTheaterService#getSurgeriesByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getSurgeriesByPatient_shouldDoNothingIfPatientIsNull() throws Exception {
		List<Surgery> result = service.getSurgeriesByPatient(null);
		assertNull(result);
		Mockito.verifyZeroInteractions(surgeryDAO);
	}

	/**
	 * @verifies call surgeryDAO getById
	 * @see OperationTheaterService#getSurgery(Integer)
	 */
	@Test
	public void getSurgery_shouldCallSurgeryDAOGetById() throws Exception {
		int id = 1;
		Surgery surgery = new Surgery();
		when(surgeryDAO.getById(id)).thenReturn(surgery);

		Surgery result = service.getSurgery(id);

		assertThat(result, is(surgery));
	}

	/**
	 * @verifies call surgeryDAO getAllUncompletedSurgeries
	 * @see OperationTheaterService#getAllUncompletedSurgeries()
	 */
	@Test
	public void getAllUncompletedSurgeries_shouldCallSurgeryDAOGetAllUncompletedSurgeries() throws Exception {
		List<Surgery> expected = new ArrayList<Surgery>();

		when(surgeryDAO.getAllUncompletedSurgeries()).thenReturn(expected);

		//call function under test
		List<Surgery> result = service.getAllUncompletedSurgeries();

		//verify
		assertThat(result, is(expected));
	}

	/**
	 * @verifies call procedureDAO saveOrUpdate if object is not null
	 * @see OperationTheaterService#retireProcedure(org.openmrs.module.operationtheater.Procedure, String)
	 */
	@Test
	public void retireProcedure_shouldCallProcedureDAOSaveOrUpdateIfObjectIsNotNull() throws Exception {
		Procedure procedure = new Procedure();

		//#1: call with null object
		//call method under test
		service.retireProcedure(null, "reason");

		//verify
		verify(procedureDAO, never()).saveOrUpdate(any(Procedure.class));

		//#2 call with proper procedure object
		service.retireProcedure(procedure, "reason");
		verify(procedureDAO).saveOrUpdate(procedure);
	}

	/**
	 * @verifies call surgeryDAO saveOrUpdate if object is not null
	 * @see OperationTheaterService#voidSurgery(org.openmrs.module.operationtheater.Surgery, String)
	 */
	@Test
	public void voidSurgery_shouldCallSurgeryDAOSaveOrUpdateIfObjectIsNotNull() throws Exception {
		Surgery surgery = new Surgery();

		//#1: call with null object
		//call method under test
		service.voidSurgery(null, "reason");

		//verify
		verify(surgeryDAO, never()).saveOrUpdate(any(Surgery.class));

		//#2 call with proper procedure object
		service.voidSurgery(surgery, "reason");
		verify(surgeryDAO).saveOrUpdate(surgery);
	}

	/**
	 * @verifies return interval from corresponding appointmentBlock
	 * @see OperationTheaterService#getLocationAvailableTime(org.openmrs.Location, org.joda.time.DateTime)
	 */
	@Test
	public void getLocationAvailableTime_shouldReturnIntervalFromCorrespondingAppointmentBlock() throws Exception {
		Location location = new Location();
		location.setName("operation theater");

		DateTime date = new DateTime();
		ArrayList<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();
		AppointmentBlock block = new AppointmentBlock();
		block.setStartDate(date.withTime(12, 30, 0, 0).toDate());
		block.setEndDate(date.withTime(13, 30, 0, 0).toDate());
		blocks.add(block);

		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		when(appointmentService
				.getAppointmentBlocks(eq(date.toDate()), eq(date.toDate()), eq(location.getId() + ","), any(Provider.class),
						any(
								AppointmentType.class))).thenReturn(blocks);

		OperationTheaterService otService = new OperationTheaterServiceImpl();
		Whitebox.setInternalState(otService, "appointmentService", appointmentService);

		//call function under test
		Interval result = otService.getLocationAvailableTime(location, date);

		//verify
		assertThat(result, notNullValue(Interval.class));
		assertThat(result.getStart(), equalTo(date.withTime(12, 30, 0, 0)));
		assertThat(result.getEnd(), equalTo(date.withTime(13, 30, 0, 0)));
	}

	/**
	 * @verifies return interval from location attribute if there is no appointmentBlock entry
	 * @see OperationTheaterService#getLocationAvailableTime(org.openmrs.Location, org.joda.time.DateTime)
	 */
	@Test
	public void getLocationAvailableTime_shouldReturnIntervalFromLocationAttributeIfThereIsNoAppointmentBlockEntry()
			throws Exception {
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		when(appointmentService.getAppointmentBlocks(any(Date.class), any(Date.class), anyString(), any(Provider.class), any(
				AppointmentType.class))).thenReturn(new ArrayList<AppointmentBlock>());

		OperationTheaterService otService = new OperationTheaterServiceImpl();
		Whitebox.setInternalState(otService, "appointmentService", appointmentService);

		Location location = new Location();
		LocationAttribute start = new LocationAttribute();
		LocationAttributeType startType = new LocationAttributeType();
		startType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
		start.setAttributeType(startType);
		start.setValue("12:30");
		LocationAttribute end = new LocationAttribute();
		LocationAttributeType endType = new LocationAttributeType();
		endType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_END_UUID);
		end.setAttributeType(endType);
		end.setValue("13:30");
		HashSet<LocationAttribute> attributes = new HashSet<LocationAttribute>();
		attributes.add(start);
		attributes.add(end);
		location.setAttributes(attributes);

		//call function under test
		Interval result = otService.getLocationAvailableTime(location, new DateTime());

		//verify
		assertThat(result, notNullValue(Interval.class));
		assertThat(result.getStart(), equalTo(new DateTime().withTime(12, 30, 0, 0)));
		assertThat(result.getEnd(), equalTo(new DateTime().withTime(13, 30, 0, 0)));
	}

	/**
	 * @verifies throw APIException if there is more than one appoitnmentBlock for this day and location
	 * @see OperationTheaterService#getLocationAvailableTime(org.openmrs.Location, org.joda.time.DateTime)
	 */
	@Test(expected = APIException.class)
	public void getLocationAvailableTime_shouldThrowAPIExceptionIfThereIsMoreThanOneAppoitnmentBlockForThisDayAndLocation()
			throws Exception {
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		ArrayList<AppointmentBlock> appointmentBlocks = new ArrayList<AppointmentBlock>();
		appointmentBlocks.add(new AppointmentBlock());
		appointmentBlocks.add(new AppointmentBlock());
		when(appointmentService.getAppointmentBlocks(any(Date.class), any(Date.class), anyString(), any(Provider.class), any(
				AppointmentType.class))).thenReturn(appointmentBlocks);

		OperationTheaterService otService = new OperationTheaterServiceImpl();
		Whitebox.setInternalState(otService, "appointmentService", appointmentService);

		//call function under test
		otService.getLocationAvailableTime(new Location(), new DateTime());
	}

	/**
	 * @verifies throw APIException if availableStart attribute is not defined
	 * @see OperationTheaterService#getLocationAvailableTime(org.openmrs.Location, org.joda.time.DateTime)
	 */
	@Test(expected = APIException.class)
	public void getLocationAvailableTime_shouldThrowAPIExceptionIfAvailableStartAttributeIsNotDefined() throws Exception {
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		when(appointmentService.getAppointmentBlocks(any(Date.class), any(Date.class), anyString(), any(Provider.class), any(
				AppointmentType.class))).thenReturn(new ArrayList<AppointmentBlock>());

		OperationTheaterService otService = new OperationTheaterServiceImpl();
		Whitebox.setInternalState(otService, "appointmentService", appointmentService);

		Location location = new Location();
		LocationAttribute start = new LocationAttribute();
		LocationAttributeType startType = new LocationAttributeType();
		startType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
		start.setAttributeType(startType);
		start.setValue("12:30");
		location.setAttribute(start);

		//call function under test
		otService.getLocationAvailableTime(location, new DateTime());
	}

	/**
	 * @verifies throw APIException if availableEnd attribute is not defined
	 * @see OperationTheaterService#getLocationAvailableTime(org.openmrs.Location, org.joda.time.DateTime)
	 */
	@Test(expected = APIException.class)
	public void getLocationAvailableTime_shouldThrowAPIExceptionIfAvailableEndAttributeIsNotDefined() throws Exception {
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		when(appointmentService.getAppointmentBlocks(any(Date.class), any(Date.class), anyString(), any(Provider.class), any(
				AppointmentType.class))).thenReturn(new ArrayList<AppointmentBlock>());

		OperationTheaterService otService = new OperationTheaterServiceImpl();
		Whitebox.setInternalState(otService, "appointmentService", appointmentService);

		Location location = new Location();
		LocationAttribute end = new LocationAttribute();
		LocationAttributeType endType = new LocationAttributeType();
		endType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_END_UUID);
		end.setAttributeType(endType);
		end.setValue("12:30");
		location.setAttribute(end);

		//call function under test
		otService.getLocationAvailableTime(location, new DateTime());
	}

	/**
	 * @verifies call surgeryDAO getScheduledSurgeries if parameter are not null
	 * @see OperationTheaterService#getScheduledSurgeries(org.joda.time.DateTime, org.joda.time.DateTime)
	 */
	@Test
	public void getScheduledSurgeries_shouldCallSurgeryDAOGetScheduledSurgeriesIfParameterAreNotNull() throws Exception {

		DateTime from = new DateTime();
		DateTime to = from.plusDays(1);

		//call function under test
		service.getScheduledSurgeries(from, to);

		//verify
		verify(surgeryDAO).getScheduledSurgeries(from, to);
	}

	/**
	 * @verifies return empty list if a parameter is null
	 * @see OperationTheaterService#getScheduledSurgeries(org.joda.time.DateTime, org.joda.time.DateTime)
	 */
	@Test
	public void getScheduledSurgeries_shouldReturnEmptyListIfAParameterIsNull() throws Exception {
		//call function under test
		List<Surgery> result = service.getScheduledSurgeries(null, new DateTime());

		//verify
		assertThat(result, hasSize(0));
		verify(surgeryDAO, never()).getScheduledSurgeries(any(DateTime.class), any(DateTime.class));

		//call function under test
		result = service.getScheduledSurgeries(new DateTime(), null);

		//verify
		assertThat(result, hasSize(0));
		verify(surgeryDAO, never()).getScheduledSurgeries(any(DateTime.class), any(DateTime.class));
	}
}
