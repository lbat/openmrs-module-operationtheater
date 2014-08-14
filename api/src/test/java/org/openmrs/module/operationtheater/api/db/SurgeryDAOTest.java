package org.openmrs.module.operationtheater.api.db;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.operation.Operation;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.DbUtil;
import org.openmrs.module.operationtheater.DbUtilDefaultInserts;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openmrs.module.operationtheater.DbUtil.insertInto;

/**
 * Tests {@link ProcedureDAO}.
 */
public class SurgeryDAOTest extends BaseModuleContextSensitiveTest {

	public static final int TOTAL_SURGERIES = 5;

	private static DbSetupTracker dbSetupTracker = new DbSetupTracker();

	@Autowired
	SurgeryDAO surgeryDAO;

	private PatientService service;

	private DateTime refDate = new DateTime().withTimeAtStartOfDay();

	@Before
	public void setUp() throws Exception {
		service = Context.getPatientService();
	}

	public void setUpDb() throws Exception {
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(DbUtil.Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Appendectomy", 35, 25, 4)
						.values("Test Surgery", 35, 25, 4)
						.build(),
				insertInto(DbUtil.Config.SCHEDULING_DATA)
						.columns("start", "end", "location_id")
						.values(refDate.minusDays(2).toDate(), refDate.minusDays(2).plusHours(1).toDate(), 100)
						.values(refDate.plusDays(2).toDate(), refDate.plusDays(2).plusHours(1).toDate(), 100)
						.values(refDate.toDate(), refDate.plusHours(1).toDate(), 100)
						.build(),
				insertInto(DbUtil.Config.SURGERY, "voided")
						.columns("patient_id", "procedure_id", "scheduling_data_id", "voided", "date_finished")
						.values(100, 1, 1, false, new DateTime().toDate())
						.values(100, 1, 2, true, null)
						.values(101, 2, 3, false, null)
						.values(101, 2, 3, true, null)
						.values(101, 2, null, false, null)
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	/**
	 * @verifies save new entry if object is not null
	 * @see org.openmrs.module.operationtheater.api.db.GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldSaveNewEntryIfObjectIsNotNull() throws Exception {
		setUpDb();

		Surgery surgery = new Surgery();
		Patient patient = service.getPatient(100);
		surgery.setPatient(patient);
		Procedure procedure = new Procedure();
		procedure.setId(1);
		surgery.setProcedure(procedure);

		Surgery savedSurgery = surgeryDAO.saveOrUpdate(surgery);
		assertThat(savedSurgery.getId(), is(TOTAL_SURGERIES + 1));

		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES + 1));

		Surgery actualSurgery = surgeryList.get(TOTAL_SURGERIES);
		assertThat(actualSurgery.getSurgeryId(), greaterThan(0));
		assertEquals(patient, actualSurgery.getPatient());
		assertEquals(procedure, actualSurgery.getProcedure());
	}

	/**
	 * @verifies not save object if it is null
	 * @see org.openmrs.module.operationtheater.api.db.GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldNotSaveObjectIfItIsNull() throws Exception {
		setUpDb();

		try {
			surgeryDAO.saveOrUpdate(null);
			fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}

		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));
	}

	/**
	 * @verifies update object if it is not null and id already in the db
	 * @see org.openmrs.module.operationtheater.api.db.GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldUpdateObjectIfItIsNotNullAndIdAlreadyInTheDb() throws Exception {
		setUpDb();

		Surgery surgery = new Surgery();
		int id = 1;
		surgery.setSurgeryId(id);
		Procedure procedure = new Procedure();
		procedure.setId(1);
		surgery.setProcedure(procedure);

		Patient patient = service.getPatient(2);
		surgery.setPatient(patient);

		surgeryDAO.saveOrUpdate(surgery);

		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));

		Surgery actualSurgery = surgeryList.get(id - 1);
		assertEquals(surgery.getSurgeryId(), actualSurgery.getSurgeryId());
		assertEquals(patient, actualSurgery.getPatient());
		assertEquals(procedure, actualSurgery.getProcedure());
	}

	@Test
	@Verifies(value = "should return all entries in the table", method = "getAll")
	public void getAll_shouldReturnAllEntriesInTheTable() throws Exception {
		setUpDb();

		//call method under test
		List<Surgery> surgeryList = surgeryDAO.getAll();

		//verify
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));
	}

	@Test
	@Verifies(value = "should return the object with the specified uuid", method = "getByUuid")
	public void getByUuid_shouldReturnTheObjectWithTheSpecifiedUuid() throws Exception {
		setUpDb();

		String uuid = "surgery1";

		//call method under test
		Surgery surgery = surgeryDAO.getByUuid(uuid);

		//verify
		assertThat(surgery.getId(), is(1));
		assertThat(surgery.getPatient().getId(), is(100));
	}

	/**
	 * @verifies return all unvoided surgery entries for this patient
	 * @see SurgeryDAO#getSurgeriesByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getSurgeriesByPatient_shouldReturnAllUnvoidedSurgeryEntriesForThisPatient() throws Exception {
		setUpDb();

		Patient patient = new Patient();
		int id = 100;
		patient.setId(id);

		//call method under test
		List<Surgery> surgeryList = surgeryDAO.getSurgeriesByPatient(patient);

		//verify
		assertThat(surgeryList, hasSize(1));
		assertThat(surgeryList.get(0).getPatient().getId(), is(id));
		assertThat(surgeryList.get(0).getSurgeryId(), is(1));
	}

	/**
	 * @verifies return all unvoided surgeries in the db that have not yet been performed
	 * @see SurgeryDAO#getAllUncompletedSurgeries()
	 */
	@Test
	public void getAllUncompletedSurgeries_shouldReturnAllUnvoidedSurgeriesInTheDbThatHaveNotYetBeenPerformed()
			throws Exception {

		setUpDb();

		//call method under test
		List<Surgery> surgeryList = surgeryDAO.getAllUncompletedSurgeries();

		//verify
		assertThat(surgeryList, hasSize(2));
		assertThat(surgeryList.get(0).getId(), is(3));
		assertThat(surgeryList.get(1).getId(), is(5));
	}

	/**
	 * @verifies return all unvoided surgeries that are scheduled between from and to date
	 * @see SurgeryDAO#getScheduledSurgeries(org.joda.time.DateTime, org.joda.time.DateTime)
	 */
	@Test
	public void getScheduledSurgeries_shouldReturnAllUnvoidedSurgeriesThatAreScheduledBetweenFromAndToDate()
			throws Exception {
		setUpDb();

		//call function under test
		List<Surgery> surgeryList = surgeryDAO.getScheduledSurgeries(refDate, refDate.plusDays(1));

		//verify
		assertThat(surgeryList, hasSize(1));
		assertThat(surgeryList.get(0).getId(), is(3));
	}

	/**
	 * @verifies return all unvoided surgeries that are started before dateTime but are not finished
	 * @see SurgeryDAO#getAllOngoingSurgeries(org.joda.time.DateTime)
	 */
	@Test
	public void getAllOngoingSurgeries_shouldReturnAllUnvoidedSurgeriesThatAreStartedBeforeDateTimeButAreNotFinished()
			throws Exception {

		//prepare
		Operation operation = sequenceOf(
				DbUtilDefaultInserts.get(),
				insertInto(DbUtil.Config.PROCEDURE)
						.columns("name", "intervention_duration", "ot_preparation_duration", "inpatient_stay")
						.values("Test Surgery", 35, 25, 4)
						.build(),
				insertInto(DbUtil.Config.SURGERY, "voided")
						.columns("patient_id", "procedure_id", "voided", "date_started", "date_finished")
						.values(100, 1, false, refDate.plusHours(11).toDate(), refDate.plusHours(13).toDate())
						.values(100, 1, false, refDate.plusHours(11).toDate(),
								refDate.plusHours(11).plusMinutes(45).toDate())
						.values(100, 1, false, refDate.plusHours(11).toDate(), null)
						.values(100, 1, true, refDate.plusHours(11).toDate(), refDate.plusHours(13).toDate())
						.build()
		);
		DbSetup dbSetup = DbUtil.buildDBSetup(operation, getConnection(), useInMemoryDatabase());
		dbSetupTracker.launchIfNecessary(dbSetup);

		//call method under test
		List<Surgery> surgeries = surgeryDAO.getAllOngoingSurgeries(refDate.plusHours(12));

		//verify
		assertThat(surgeries, hasSize(2));
		assertThat(surgeries.get(0).getId(), is(1));
		assertThat(surgeries.get(1).getId(), is(3));
	}

	/**
	 * @verifies return empty list if dateTime is null
	 * @see SurgeryDAO#getAllOngoingSurgeries(org.joda.time.DateTime)
	 */
	@Test
	public void getAllOngoingSurgeries_shouldReturnEmptyListIfDateTimeIsNull() throws Exception {

		//call method under test
		List<Surgery> surgeries = surgeryDAO.getAllOngoingSurgeries(null);

		//verify
		assertThat(surgeries, hasSize(0));
	}
}
