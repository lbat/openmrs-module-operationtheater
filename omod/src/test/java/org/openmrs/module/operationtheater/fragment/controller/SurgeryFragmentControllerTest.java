package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SurgeryFragmentController}
 */
public class SurgeryFragmentControllerTest {

	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see SurgeryFragmentController#getSurgicalTeam(UiUtils, Surgery)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getSurgicalTeam_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		//call method under test
		new SurgeryFragmentController().getSurgicalTeam(new TestUiUtils(), null);
	}

	/**
	 * @verifies return names of providers that are assigned to this surgery
	 * @see SurgeryFragmentController#getSurgicalTeam(UiUtils, Surgery)
	 */
	@Test
	public void getSurgicalTeam_shouldReturnNamesOfProvidersThatAreAssignedToThisSurgery() throws Exception {

		Surgery surgery = new Surgery();
		Set<Provider> surgicalTeam = new HashSet<Provider>();
		Provider provider1 = new Provider();
		provider1.setName("name1");
		provider1.setUuid("uuid1");
		Provider provider2 = new Provider();
		provider2.setName("name2");
		provider2.setUuid("uuid2");

		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(new Provider[] { provider1, provider2 })));

		//call method under test
		List<SimpleObject> result = new SurgeryFragmentController().getSurgicalTeam(new TestUiUtils(), surgery);

		//verify
		assertThat(result, hasSize(2));
		List<String> jsonList = new ArrayList<String>();
		jsonList.add(result.get(0).toJson());
		jsonList.add(result.get(1).toJson());

		assertThat(jsonList, containsInAnyOrder(
				new String[] { "{\"name\":\"name1\",\"uuid\":\"uuid1\"}", "{\"name\":\"name2\",\"uuid\":\"uuid2\"}" }));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(new Provider());

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), null, uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is null
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfProviderIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.provider.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is already part of surgical team
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, String, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.api.ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfProviderIsAlreadyPartOfSurgicalTeam() throws Exception {
		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		Provider provider = new Provider();
		provider.setUuid(uuid);
		provider.setName("name");
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		Surgery surgery = new Surgery();
		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(provider)));

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), surgery, uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is("operationtheater.surgery.providerAlreadyPartOfSurgicalTeam:name"));
	}

	/**
	 * @verifies return SuccessResult if provider has been added
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnSuccessResultIfProviderHasBeenAdded() throws Exception {

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Provider provider = new Provider();
		String providerUuid = "uuid";
		provider.setName("name");

		ProviderService providerService = Mockito.mock(ProviderService.class);
		when(providerService.getProviderByUuid(providerUuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), new Surgery(), providerUuid, service, providerService);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		assertThat(captor.getValue().getSurgicalTeam(), hasSize(1));
		assertTrue(captor.getValue().getSurgicalTeam().contains(provider));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.addedProviderToSurgicalTeam:name"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), null, "uuid", null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is null
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfProviderIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.provider.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is not part of surgical team
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfProviderIsNotPartOfSurgicalTeam()
			throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		Provider provider = new Provider();
		provider.setName("name");
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is("operationtheater.surgery.providerNotPartOfSurgicalTeam:name"));
	}

	/**
	 * @verifies return SuccessResult if provider has been removed
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnSuccessResultIfProviderHasBeenRemoved() throws Exception {
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Surgery surgery = new Surgery();
		Provider provider = new Provider();
		String uuid = "uuid";
		provider.setName("name");
		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(provider)));

		ProviderService providerService = Mockito.mock(ProviderService.class);
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), surgery, uuid, service, providerService);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		assertThat(captor.getValue().getSurgicalTeam(), hasSize(0));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.removedProviderFromSurgicalTeam:name"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), null, new Procedure(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if procedure is null
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnFailureResultIfProcedureIsNull() throws Exception {

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), new Surgery(), null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.procedure.notFound"));
	}

	/**
	 * @verifies return SuccessResult if procedure has been successfully updated
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnSuccessResultIfProcedureHasBeenSuccessfullyUpdated() throws Exception {

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Procedure procedure = new Procedure();
		procedure.setUuid("procedureUuid");
		procedure.setName("procedureName");

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), new Surgery(), procedure, service);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());
		assertThat(captor.getValue().getProcedure(), is(procedure));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.updated.procedure:procedureName"));
	}
}
