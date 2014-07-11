package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ManageProceduresFragmentController}
 */
public class ManageProceduresFragmentControllerTest {

	/**
	 * @verifies retire procedure and return success result
	 * @see ManageProceduresFragmentController#retireProcedure(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void retireProcedure_shouldRetireProcedureAndReturnSuccessResult() throws Exception {
		UiUtils ui = new TestUiUtils();
		Procedure procedure = new Procedure();
		int procedureId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getProcedure(procedureId)).thenReturn(procedure);
		when(service.retireProcedure(eq(procedure), anyString())).thenReturn(null);

		//Call function under test
		FragmentActionResult result = new ManageProceduresFragmentController().retireProcedure(ui, procedureId, service);

		//verify
		verify(service).retireProcedure(eq(procedure), anyString());
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is(ui.message("operationtheater.procedure.retiredSuccessfully")));
	}

	/**
	 * @verifies return failure result if no procedure with given primary key is found in the db
	 * @see ManageProceduresFragmentController#retireProcedure(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void retireProcedure_shouldReturnFailureResultIfNoProcedureWithGivenPrimaryKeyIsFoundInTheDb() throws Exception {
		UiUtils ui = new TestUiUtils();
		int procedureId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getProcedure(procedureId)).thenReturn(null);

		//Call function under test
		FragmentActionResult result = new ManageProceduresFragmentController().retireProcedure(ui, procedureId, service);

		//verify
		verify(service, never()).retireProcedure(any(Procedure.class), anyString());
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is(ui.message("operationtheater.procedure.notFound")));
	}

	/**
	 * @verifies return failure result if user is not allowed to retire a procedure
	 * @see ManageProceduresFragmentController#retireProcedure(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void retireProcedure_shouldReturnFailureResultIfUserIsNotAllowedToRetireAProcedure() throws Exception {
		UiUtils ui = new TestUiUtils();
		Procedure procedure = new Procedure();
		int procedureId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getProcedure(procedureId)).thenReturn(procedure);
		when(service.retireProcedure(eq(procedure), anyString())).thenThrow(new APIAuthenticationException());

		//Call function under test
		FragmentActionResult result = new ManageProceduresFragmentController().retireProcedure(ui, procedureId, service);

		//verify
		verify(service).retireProcedure(eq(procedure), anyString());
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is(ui.message("operationtheater.procedure.retire.notAllowed")));
	}
}
