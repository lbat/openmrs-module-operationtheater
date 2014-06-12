package org.openmrs.module.operationtheater.page.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.page.PageModel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link CreateEditProcedurePageController}
 */
public class CreateEditProcedurePageControllerTest {

	@Mock
	public OperationTheaterService otService;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * @verifies get procedure from db if parameter id is not null
	 * @see CreateEditProcedurePageController#get(org.openmrs.ui.framework.page.PageModel, String, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void get_shouldGetProcedureFromDbIfParameterIdIsNotNull() throws Exception {
		Integer id = 1;

		Procedure procedure = new Procedure();

		doReturn(procedure).when(otService).getProcedure(id);

		PageModel model = new PageModel();
		new CreateEditProcedurePageController().get(model, id, otService);

		assertThat((Procedure) model.getAttribute("procedure"), is(procedure));
	}

	/**
	 * @verifies add empty procedure to the model
	 * @see CreateEditProcedurePageController#get(org.openmrs.ui.framework.page.PageModel, String, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void get_shouldAddEmptyProcedureToTheModel() throws Exception {
		Integer id = null;

		PageModel model = new PageModel();
		new CreateEditProcedurePageController().get(model, id, otService);

		verify(otService, never()).getProcedureByUuid(anyString());
		assertNotNull(model.getAttribute("procedure"));
	}
}
