package org.openmrs.module.operationtheater.rest.resources.openmrs1_9;

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

import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Tests {@link SurgeryResource1_9}
 */
public class SurgeryResource1_9Test extends BaseDelegatingResourceTest<SurgeryResource1_9, Surgery> {

	public static final String VOIDED_SURGERY_UUID = "ca352fc1-1691-11df-97a5-7038c432aab6";

	public static final String SURGERY_UUID = "ca352fc1-1691-11df-97a5-7038c432aab5";

	@Before
	public void setUp() throws Exception {
		executeDataSet("standardOperationTheaterTestDataset.xml");
	}

	@Override
	public Surgery newObject() {
		return Context.getService(OperationTheaterService.class).getSurgeryByUuid(getUuidProperty());
	}

	@Override
	public void validateRefRepresentation() throws Exception {
		super.validateRefRepresentation();
		assertPropEquals("voided",
				getObject().isVoided()); // note that the voided property is only present if the property is voided
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("patient");
		assertPropEquals("voided", getObject().getVoided());
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("patient");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}

	@Override
	public String getDisplayProperty() {
		//FIXME display string
		return "this is the display string";
	}

	@Override
	public String getUuidProperty() {
		return VOIDED_SURGERY_UUID;
	}
}
