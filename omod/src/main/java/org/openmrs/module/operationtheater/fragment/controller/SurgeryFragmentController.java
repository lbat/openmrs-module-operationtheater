package org.openmrs.module.operationtheater.fragment.controller;

import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.validator.SurgeryValidator;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurgeryFragmentController {

	private final Time time = new Time();

	/**
	 * @param ui
	 * @param surgery
	 * @return
	 * @should throw IllegalArgumentException if surgery is null
	 * @should return names of providers that are assigned to this surgery
	 */
	public List<SimpleObject> getSurgicalTeam(UiUtils ui, @RequestParam("surgery") Surgery surgery) {
		if (surgery == null) {
			throw new IllegalArgumentException("Surgery doesn't exist");
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

	/**
	 * @param ui
	 * @param surgeryUuid
	 * @param patient
	 * @param procedure
	 * @param otService
	 * @return
	 * @should return FailureResult if surgeryUuid is null
	 * @should return FailureResult if patient is null
	 * @should return FailureResult if procedure is null
	 * @should return FailureResult if surgery already exists
	 * @should return SuccessResult if surgery has been created successfully
	 */
	public FragmentActionResult createNewSurgery(UiUtils ui,
	                                             @RequestParam("surgery") String surgeryUuid,
	                                             @RequestParam("patient") Patient patient,
	                                             @RequestParam("procedure") Procedure procedure,
	                                             @SpringBean OperationTheaterService otService) {
		if (surgeryUuid == null) {
			return new FailureResult(ui.message("operationtheater.surgery.nullUuid"));
		}
		if (patient == null) {
			return new FailureResult(ui.message("operationtheater.patient.notFound"));
		}
		if (procedure == null) {
			return new FailureResult(ui.message("operationtheater.procedure.notFound"));
		}
		if (otService.getSurgeryByUuid(surgeryUuid) != null) {
			return new FailureResult(ui.message("operationtheater.surgery.alreadyExists"));
		}
		Surgery surgery = new Surgery();
		surgery.setUuid(surgeryUuid);
		surgery.setPatient(patient);
		surgery.setProcedure(procedure);

		otService.saveSurgery(surgery);
		return new SuccessResult(ui.message("operationtheater.surgery.createdSuccessfully"));
	}

	/**
	 * @param ui
	 * @param surgery
	 * @return
	 * @should throw IllegalArgumentException if surgery is null
	 * @should return all surgery times of this surgery if it has already been finished
	 * @should return created and start times of this surgery if it hasn't been finished
	 * @should return created time of this surgery if it hasn't been started
	 */
	public List<SimpleObject> getSurgeryTimes(UiUtils ui, @RequestParam("surgery") Surgery surgery) {
		if (surgery == null) {
			throw new IllegalArgumentException("Surgery doesn't exist");
		}
		List<SurgeryTime> surgeryTimes = new ArrayList<SurgeryTime>();

		//date created
		if (surgery.getDateCreated() != null) {
			String dateCreated = OTMetadata.DATE_TIME_FORMATTER.print(new DateTime(surgery.getDateCreated()));
			String displayName = ui.message("operationtheater.surgery.dateCreated.displayName");
			surgeryTimes.add(new SurgeryTime(SurgeryTimeType.CREATED, displayName, dateCreated));
		}

		//date started
		if (surgery.getDateStarted() != null) {
			String dateStarted = OTMetadata.DATE_TIME_FORMATTER.print(surgery.getDateStarted());
			String displayName = ui.message("operationtheater.surgery.dateStarted.displayName");
			surgeryTimes.add(new SurgeryTime(SurgeryTimeType.STARTED, displayName, dateStarted));
		}

		//date finished
		if (surgery.getDateFinished() != null) {
			String dateFinished = OTMetadata.DATE_TIME_FORMATTER.print(surgery.getDateFinished());
			String displayName = ui.message("operationtheater.surgery.dateFinished.displayName");
			surgeryTimes.add(new SurgeryTime(SurgeryTimeType.FINISHED, displayName, dateFinished));
		}

		return SimpleObject.fromCollection(surgeryTimes, ui, "type", "displayName", "dateTimeStr");
	}

	/**
	 * @param ui
	 * @param surgery
	 * @param otService
	 * @param validator
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if surgery has already been finished
	 * @should return FailureResult if surgery has already been started
	 * @should return FailureResult if validation fails
	 * @should return SuccessResult if dateStarted has been successfully set
	 */
	public FragmentActionResult startSurgery(UiUtils ui, @RequestParam("surgery") Surgery surgery,
	                                         @SpringBean OperationTheaterService otService,
	                                         @SpringBean SurgeryValidator validator) {
		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (surgery.getDateFinished() != null) {
			return new FailureResult(ui.message("operationtheater.surgery.alreadyFinished"));
		}
		if (surgery.getDateStarted() != null) {
			return new FailureResult(ui.message("operationtheater.surgery.alreadyStarted"));
		}

		surgery.setDateStarted(time.now());

		//validate
		Errors errors = new BindException(surgery, "surgery");
		validator.validate(surgery, errors);
		if (errors.hasErrors()) {
			return new FailureResult(errors);
		}
		otService.saveSurgery(surgery);
		String otName = surgery.getSchedulingData().getLocation().getName();
		return new SuccessResult(ui.message("operationtheater.surgery.started", otName));
	}

	/**
	 * @param ui
	 * @param surgery
	 * @param otService
	 * @param validator
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if validation fails
	 * @should return FailureResult if surgery has already been finished
	 * @should return SuccessResult if dateFinished has been successfully set
	 */
	public FragmentActionResult finishSurgery(UiUtils ui, @RequestParam("surgery") Surgery surgery,
	                                          @SpringBean OperationTheaterService otService,
	                                          @SpringBean SurgeryValidator validator) {
		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (surgery.getDateFinished() != null) {
			return new FailureResult(ui.message("operationtheater.surgery.alreadyFinished"));
		}

		surgery.setDateFinished(time.now());

		//validate
		Errors errors = new BindException(surgery, "surgery");
		validator.validate(surgery, errors);
		if (errors.hasErrors()) {
			return new FailureResult(errors);
		}
		otService.saveSurgery(surgery);
		return new SuccessResult(ui.message("operationtheater.surgery.finished"));
	}

	/**
	 * replaces the placeholder emergency patient with the given patient
	 *
	 * @param ui
	 * @param surgery
	 * @param patient
	 * @param otService
	 * @return
	 * @should return FailureResult if surgery is null
	 * @should return FailureResult if patient is null
	 * @should return FailureResult if current patient is not the emergency placeholder patient
	 * @should return SuccessResult if replacement was successful
	 */
	public FragmentActionResult replaceEmergencyPlaceholderPatient(UiUtils ui,
	                                                               @RequestParam("surgery") Surgery surgery,
	                                                               @RequestParam("patient") Patient patient,
	                                                               @SpringBean OperationTheaterService otService) {
		if (surgery == null) {
			return new FailureResult(ui.message("operationtheater.surgery.notFound"));
		}
		if (patient == null) {
			return new FailureResult(ui.message("operationtheater.patient.notFound"));
		}

		Patient currentPatient = surgery.getPatient();
		if (!currentPatient.getUuid().equals(OTMetadata.PLACEHOLDER_PATIENT_UUID)) {
			return new FailureResult(ui.message("operationtheater.surgery.notPlaceholderPatient",
					currentPatient.getFamilyName() + ", " + currentPatient.getGivenName()));
		}

		surgery.setPatient(patient);
		otService.saveSurgery(surgery);

		return new SuccessResult(ui.message("operationtheater.surgery.replacedPlaceholderPatientSuccessfully"));
	}

	static enum SurgeryTimeType {CREATED, STARTED, FINISHED}

	static class SurgeryTime {

		public SurgeryTimeType type;

		public String displayName = "";

		public String dateTimeStr;

		SurgeryTime(SurgeryTimeType type, String displayName, String dateTimeStr) {
			this.type = type;
			this.displayName = displayName;
			this.dateTimeStr = dateTimeStr;
		}

		public SurgeryTimeType getType() {
			return type;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getDateTimeStr() {
			return dateTimeStr;
		}
	}
}
