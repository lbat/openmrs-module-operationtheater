package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.Surgery;
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
 * Tests {@link PatientsSurgeriesFragmentController}
 */
public class PatientsSurgeriesFragmentControllerTest {

	/**
	 * @verifies retire surgery and return success result
	 * @see PatientsSurgeriesFragmentController#voidSurgery(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void voidSurgery_shouldRetireSurgeryAndReturnSuccessResult() throws Exception {
		UiUtils ui = new TestUiUtils();
		Surgery surgery = new Surgery();
		int surgeryId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getSurgery(surgeryId)).thenReturn(surgery);
		when(service.voidSurgery(eq(surgery), anyString())).thenReturn(null);

		//Call function under test
		FragmentActionResult result = new PatientsSurgeriesFragmentController().voidSurgery(ui, surgeryId, service);

		//verify
		verify(service).voidSurgery(eq(surgery), anyString());
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is(ui.message("operationtheater.surgery.voidedSuccessfully")));
	}

	/**
	 * @verifies return failure result if no surgery with given primary key is found in the db
	 * @see PatientsSurgeriesFragmentController#voidSurgery(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void voidSurgery_shouldReturnFailureResultIfNoSurgeryWithGivenPrimaryKeyIsFoundInTheDb() throws Exception {
		UiUtils ui = new TestUiUtils();
		int surgeryId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getSurgery(surgeryId)).thenReturn(null);

		//Call function under test
		FragmentActionResult result = new PatientsSurgeriesFragmentController().voidSurgery(ui, surgeryId, service);

		//verify
		verify(service, never()).voidSurgery(any(Surgery.class), anyString());
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is(ui.message("operationtheater.surgery.notFound")));
	}

	/**
	 * @verifies return failure result if user is not allowed to retire a surgery
	 * @see PatientsSurgeriesFragmentController#voidSurgery(org.openmrs.ui.framework.UiUtils, int, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void voidSurgery_shouldReturnFailureResultIfUserIsNotAllowedToRetireASurgery() throws Exception {
		UiUtils ui = new TestUiUtils();
		Surgery surgery = new Surgery();
		int surgeryId = 1;

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.getSurgery(surgeryId)).thenReturn(surgery);
		when(service.voidSurgery(eq(surgery), anyString())).thenThrow(new APIAuthenticationException());

		//Call function under test
		FragmentActionResult result = new PatientsSurgeriesFragmentController().voidSurgery(ui, surgeryId, service);

		//verify
		verify(service).voidSurgery(eq(surgery), anyString());
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is(ui.message("operationtheater.surgery.void.notAllowed")));
	}
}
