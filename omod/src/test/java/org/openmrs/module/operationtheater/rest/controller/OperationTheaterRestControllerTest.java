package org.openmrs.module.operationtheater.rest.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * tests {@link org.openmrs.module.operationtheater.rest.controller.OperationTheaterRestController}
 */
public class OperationTheaterRestControllerTest {

	/**
	 * @verifies return v1 slash operationtheater
	 * @see OperationTheaterRestController#getNamespace()
	 */
	@Test
	public void getNamespace_shouldReturnV1SlashOperationtheater() throws Exception {
		OperationTheaterRestController controller = new OperationTheaterRestController();
		assertEquals("v1/operationtheater", controller.getNamespace());
	}
}
