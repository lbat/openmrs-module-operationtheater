package org.openmrs.module.operationtheater.fragment.controller;

import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurgeryFragmentController {

	/**
	 * @param ui
	 * @param surgery
	 * @return
	 * @should throw IllegalArgumentException if surgery is null
	 * @should return names of providers that are assigned to this surgery
	 */
	public List<SimpleObject> getSurgicalTeam(UiUtils ui, @RequestParam("surgery") Surgery surgery) {
		if (surgery == null) {
			throw new IllegalArgumentException("");
		}
		return SimpleObject.fromCollection(surgery.getSurgicalTeam(), ui, "name", "uuid");
	}

	/**
	 * @param ui
	 * @param surgery
	 * @param providerUuid
	 * @param otService
	 * @param providerService
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if provider is null
	 * @should return FailureResult if provider is already part of surgical team
	 * @should return SuccessResult if provider has been added
	 */
	public FragmentActionResult addProviderToSurgicalTeam(UiUtils ui,
	                                                      @RequestParam("surgery") Surgery surgery,
	                                                      @RequestParam("provider") String providerUuid,
	                                                      @SpringBean OperationTheaterService otService,
	                                                      @SpringBean("providerService") ProviderService providerService) {
		//EMR Api already defines StringToProviderConverter that cannot deal with uuids
		Provider provider = providerService.getProviderByUuid(providerUuid);

		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (provider == null) {
			return new FailureResult(ui.message("operationtheater.provider.notFound"));
		}

		Set<Provider> providers = surgery.getSurgicalTeam();
		if (providers == null) {
			providers = new HashSet<Provider>();
		} else if (providers.contains(provider)) {
			return new FailureResult(ui.message("operationtheater.surgery.providerAlreadyPartOfSurgicalTeam", provider));
		}
		providers.add(provider);
		surgery.setSurgicalTeam(providers);
		otService.saveSurgery(surgery);
		return new SuccessResult(ui.message("operationtheater.surgery.addedProviderToSurgicalTeam", provider));
	}

	/**
	 * @param ui
	 * @param surgery
	 * @param providerUuid
	 * @param otService
	 * @param providerService
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if provider is null
	 * @should return FailureResult if provider is not part of surgical team
	 * @should return SuccessResult if provider has been removed
	 */
	public FragmentActionResult removeProviderFromSurgicalTeam(UiUtils ui,
	                                                           @RequestParam("surgery") Surgery surgery,
	                                                           @RequestParam("provider") String providerUuid,
	                                                           @SpringBean OperationTheaterService otService,
	                                                           @SpringBean(
			                                                           "providerService") ProviderService providerService) {
		//EMR Api already defines StringToProviderConverter that cannot deal with uuids
		Provider provider = providerService.getProviderByUuid(providerUuid);
		System.err.println("test");
		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (provider == null) {
			return new FailureResult(ui.message("operationtheater.provider.notFound"));
		}

		Set<Provider> providers = surgery.getSurgicalTeam();
		if (providers == null || !providers.contains(provider)) {
			return new FailureResult(ui.message("operationtheater.surgery.providerNotPartOfSurgicalTeam", provider));
		}
		providers.remove(provider);
		surgery.setSurgicalTeam(providers);
		otService.saveSurgery(surgery);
		return new SuccessResult(ui.message("operationtheater.surgery.removedProviderFromSurgicalTeam", provider));
	}

	/**
	 * @param ui
	 * @param surgery
	 * @param procedure
	 * @param otService
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if procedure is null
	 * @should return SuccessResult if procedure has been successfully updated
	 */
	public FragmentActionResult updateProcedure(UiUtils ui,
	                                            @RequestParam("surgery") Surgery surgery,
	                                            @RequestParam("procedure") Procedure procedure,
	                                            @SpringBean OperationTheaterService otService) {
		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (procedure == null) {
			return new FailureResult(ui.message("operationtheater.procedure.notFound"));
		}

		surgery.setProcedure(procedure);

		//FIXME update scheduling data
		//FIXME trigger automatic scheduler

		otService.saveSurgery(surgery);
		return new SuccessResult(ui.message("operationtheater.surgery.updated.procedure", procedure.getName()));
	}
}
