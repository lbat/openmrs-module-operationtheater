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
import static org.junit.Assert.assertEquals;
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

		Mockito.verify(surgeryDAO).saveOrUpdate(surgery);
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

		Mockito.verify(procedureDAO).saveOrUpdate(procedure);
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
		//		Mockito.verify(surgeryDAO).getAllData(true);

		when(surgeryDAO.getAllData(false)).thenReturn(surgeryList);
		result = service.getAllSurgeries(false);
		assertEquals(surgeryList, result);
		//		Mockito.verify(surgeryDAO).getAllData(false);
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
}
