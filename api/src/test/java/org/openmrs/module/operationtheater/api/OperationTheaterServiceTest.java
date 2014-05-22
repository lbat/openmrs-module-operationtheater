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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateProcedureDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateSurgeryDAO;
import org.openmrs.module.operationtheater.api.impl.OperationTheaterServiceImpl;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Tests {@link ${OperationTheaterService}}.
 */
public class  OperationTheaterServiceTest extends BaseModuleContextSensitiveTest {

	private OperationTheaterService service;

	private SurgeryDAO surgeryDAO;

	private ProcedureDAO procedureDAO;

	@Before
	public void setUp(){
		service = new OperationTheaterServiceImpl();
		surgeryDAO = Mockito.mock(HibernateSurgeryDAO.class);
		service.setSurgeryDAO(surgeryDAO);
		procedureDAO = Mockito.mock(HibernateProcedureDAO.class);
		service.setProcedureDAO(procedureDAO);
	}

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(OperationTheaterService.class));
	}

	@Test
	@Verifies(value="should call surgeryDao saveOrUpdate", method="saveSurgery(Surgery)")
	public void saveSurgery_shouldCallSurgeryDAOSaveOrUpdate() throws Exception {
		Surgery surgery = new Surgery();
		service.saveSurgery(surgery);
		Mockito.verify(surgeryDAO).saveOrUpdate(surgery);
	}

	@Test
	@Verifies(value = "should call procedureDao saveOrUpdate", method = "saveProcedure()")
	public void saveProcedure_shouldCreateNewDbEntryIfObjectIsNotNull() throws Exception {
		Procedure procedure = new Procedure();
		service.saveProcedure(procedure);
		Mockito.verify(procedureDAO).saveOrUpdate(procedure);
	}
}
