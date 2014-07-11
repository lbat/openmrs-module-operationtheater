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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.Patient;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
		verify(procedureDAO, never()).saveOrUpdate(Mockito.any(Procedure.class));

		//#2 call with proper procedure object
		service.retireProcedure(procedure, "reason");
		verify(procedureDAO).saveOrUpdate(procedure);
	}
}
