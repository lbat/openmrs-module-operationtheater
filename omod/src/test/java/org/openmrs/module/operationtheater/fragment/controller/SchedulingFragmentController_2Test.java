package org.openmrs.module.operationtheater.fragment.controller;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.operation.Operation;
import org.databene.commons.db.DBUtil;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.DbUtil;
import org.openmrs.module.operationtheater.DbUtilDefaultInserts;
import org.openmrs.module.operationtheater.MockUtil;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.OperationTheater;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.Scheduler;
import org.openmrs.module.operationtheater.validator.SchedulingDataValidator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.validation.Validator;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.operationtheater.DbUtil.Config;
import static org.openmrs.module.operationtheater.DbUtil.insertInto;

/**
 * Tests {@link SchedulingFragmentController}, but doesn't use Powermock and extends BaseModuleContextSensitiveTest
 */
public class SchedulingFragmentController_2Test extends BaseModuleContextSensitiveTest {

//	private static DbSetupTracker dbSetupTracker = new DbSetupTracker();

	private DateTime refDate = new DateTime().withTimeAtStartOfDay();

	private DateTime now;

	private Time timeMock;

	@Before
	public void setUp() {
		timeMock =  Mockito.mock(Time.class);
		now = refDate.withTime(11, 0, 0, 0);
		when(timeMock.now()).thenReturn(now);
	}

	@After
	public void tearDown() {
		Config.PATIENT.setPkStartValue(100);
		Config.PERSON.setPkStartValue(100);
	}

	private void mockValidateUtil(SchedulingFragmentController controller, final boolean validationShouldPass)
			throws Exception {
		Validator validator = MockUtil
				.mockValidator(validationShouldPass, SchedulingDataValidator.class, SchedulingData.class, "field", "code");
		Whitebox.setInternalState(controller, "schedulingDataValidator", validator);
	}

	/**
	 * @verifies create a surgery record with scheduled start time is now if there is a free operation theater
	 * @see SchedulingFragmentController#scheduleEmergency(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.api.LocationService, org.openmrs.api.PatientService)
	 */
	@Test
	public void scheduleEmergency_shouldCreateASurgeryRecordWithScheduledStartTimeIsNowIfThereIsAFreeOperationTheater()
			throws Exception {
		//prepare
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE, "uuid")
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay", "uuid")
						.values("Some surgery", 35, 25, 4, "procedure1")
						.values("Placeholder", 35, 25, 4, OTMetadata.PLACEHOLDER_PROCEDURE_UUID)
						.build(),
				DbUtil.insertInto(Config.PERSON.setPkStartValue(102), "uuid").columns("uuid").values(OTMetadata.PLACEHOLDER_PATIENT_UUID)
						.build(),
				DbUtil.insertInto(Config.PATIENT.setPkStartValue(102)).columns().values().build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("location_id")
						.values(100)
						.values(101)
						.values(101)
						.build(),
				insertInto(Config.SURGERY)
						.columns("patient_id", "procedure_id", "scheduling_data_id", "date_started", "date_finished")
						.values(100, 1, 1, now.minusHours(1).toDate(), null)
						.values(100, 1, 2, now.minusHours(2).toDate(), now.minusMinutes(1).toDate())
						.values(100, 1, 3, null, null)
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetup.launch();
		//		dbSetupTracker.launchIfNecessary(dbSetup);

		OperationTheaterService otService = Context.getService(OperationTheaterService.class);

		SchedulingFragmentController schedulingFragmentController = new SchedulingFragmentController();
		Whitebox.setInternalState(schedulingFragmentController, "time", timeMock);

		//call method under test
		SimpleObject result = schedulingFragmentController
				.scheduleEmergency(new TestUiUtils(), otService, Context.getLocationService(), Context.getPatientService());


		//verify
		Surgery surgery = otService.getSurgery(4);
		assertThat(surgery, is(notNullValue()));
		assertThat(surgery.getProcedure().getUuid(), is(OTMetadata.PLACEHOLDER_PROCEDURE_UUID));
		assertThat(surgery.getPatient().getUuid(), is(OTMetadata.PLACEHOLDER_PATIENT_UUID));
		SchedulingData schedulingData = surgery.getSchedulingData();
		assertThat(schedulingData.getStart(), equalTo(now));
		assertThat(schedulingData.getEnd(), equalTo(now.plusMinutes(60)));
		assertThat(schedulingData.getLocation().getId(), equalTo(101));

		assertThat((String) result.get("location"), is("OT 2"));
		assertThat((Integer) result.get("waitingTime"), is(0));
	}

	/**
	 * @verifies create a surgery record with scheduled start equal to the time the next operation theater will be available
	 * @see SchedulingFragmentController#scheduleEmergency(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.api.LocationService, org.openmrs.api.PatientService)
	 */
	@Test
	public void scheduleEmergency_shouldCreateASurgeryRecordWithScheduledStartEqualToTheTimeTheNextOperationTheaterWillBeAvailable()
			throws Exception {
		//prepare
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(Config.PROCEDURE, "uuid")
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay", "uuid")
						.values("Longer surgery", 125, 25, 4, "procedure1")
						.values("Some surgery", 35, 25, 4, "procedure2")
						.values("Placeholder", 35, 25, 4, OTMetadata.PLACEHOLDER_PROCEDURE_UUID)
						.build(),
				DbUtil.insertInto(Config.PERSON.setPkStartValue(103), "uuid").columns("uuid").values(
						OTMetadata.PLACEHOLDER_PATIENT_UUID)
						.build(),
				DbUtil.insertInto(Config.PATIENT.setPkStartValue(103)).columns().values().build(),
				insertInto(Config.SCHEDULING_DATA)
						.columns("location_id")
						.values(100)
						.values(101)
						.values(101)
						.build(),
				insertInto(Config.SURGERY)
						.columns("patient_id", "procedure_id", "scheduling_data_id", "date_started", "date_finished")
						.values(100, 1, 1, now.minusMinutes(10).toDate(), null)
						.values(100, 2, 2, now.minusMinutes(10).toDate(), null)
						.values(100, 1, 3, null, null)
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetup.launch();
//		dbSetupTracker.launchIfNecessary(dbSetup);

		OperationTheaterService otService = Context.getService(OperationTheaterService.class);

		SchedulingFragmentController schedulingFragmentController = new SchedulingFragmentController();
		Whitebox.setInternalState(schedulingFragmentController, "time", timeMock);

		//call method under test
		SimpleObject result = schedulingFragmentController
				.scheduleEmergency(new TestUiUtils(), otService, Context.getLocationService(), Context.getPatientService());


		//verify
		Surgery surgery = otService.getSurgery(5); //TODO find better solution - should be 4, but previous test influences auto increment value
		assertThat(surgery, is(notNullValue()));
		assertThat(surgery.getProcedure().getUuid(), is(OTMetadata.PLACEHOLDER_PROCEDURE_UUID));
		assertThat(surgery.getPatient().getUuid(), is(OTMetadata.PLACEHOLDER_PATIENT_UUID));
		SchedulingData schedulingData = surgery.getSchedulingData();
		assertThat(schedulingData.getStart(), equalTo(now.plusMinutes(25)));
		assertThat(schedulingData.getEnd(), equalTo(now.plusMinutes(85)));
		assertThat(schedulingData.getLocation().getId(), equalTo(101));

		assertThat((String) result.get("location"), is("OT 2"));
		assertThat((Integer) result.get("waitingTime"), is(25));
	}
}
