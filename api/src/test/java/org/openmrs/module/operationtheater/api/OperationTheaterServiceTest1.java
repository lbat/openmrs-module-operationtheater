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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateProcedureDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateSurgeryDAO;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ${OperationTheaterService}}.
 * tests that are placed here are context sensitive
 */
public class OperationTheaterServiceTest1 extends BaseModuleContextSensitiveTest {

	private OperationTheaterService service;

	private SurgeryDAO surgeryDAO;

	private ProcedureDAO procedureDAO;

	@Before
	public void setUp() {
		service = Context.getService(OperationTheaterService.class);
		surgeryDAO = Mockito.mock(HibernateSurgeryDAO.class);
		service.setSurgeryDAO(surgeryDAO);
		procedureDAO = Mockito.mock(HibernateProcedureDAO.class);
		service.setProcedureDAO(procedureDAO);
	}

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(OperationTheaterService.class));
	}

	/**
	 * test needs to be context sensitive because the actual voiding is done through AOP
	 *
	 * @verifies void the given surgery
	 * @see OperationTheaterService#voidSurgery(org.openmrs.module.operationtheater.Surgery, String)
	 */
	@Test
	public void voidSurgery_shouldVoidTheGivenSurgery() throws Exception {
		Surgery surgery = new Surgery();
		String reason = "void reason";

		ArgumentCaptor<Surgery> argumentCaptor = ArgumentCaptor.forClass(Surgery.class);
		when(surgeryDAO.saveOrUpdate(argumentCaptor.capture())).thenReturn(surgery);

		Surgery returnedSurgery = service.voidSurgery(surgery, reason);

		assertEquals(surgery, returnedSurgery);
		assertTrue(argumentCaptor.getValue().isVoided());
		assertThat(argumentCaptor.getValue().getVoidReason(), is(reason));
	}

	/**
	 * @verifies unvoid the given surgery
	 * @see OperationTheaterService#unvoidSurgery(org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void unvoidSurgery_shouldUnvoidTheGivenSurgery() throws Exception {
		Surgery surgery = new Surgery();
		surgery.setVoided(true);

		ArgumentCaptor<Surgery> argumentCaptor = ArgumentCaptor.forClass(Surgery.class);
		when(surgeryDAO.saveOrUpdate(argumentCaptor.capture())).thenReturn(surgery);

		Surgery returnedSurgery = service.unvoidSurgery(surgery);

		assertEquals(surgery, returnedSurgery);
		assertFalse(argumentCaptor.getValue().isVoided());

	}
}
