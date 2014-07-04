package org.openmrs.module.operationtheater.api.db;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link ProcedureDAO}.
 */
public class SurgeryDAOTest extends BaseModuleContextSensitiveTest {

	public static final int TOTAL_SURGERIES = 4;

	@Autowired
	SurgeryDAO surgeryDAO;

	private PatientService service;

	@Before
	public void setUp() throws Exception {
		executeDataSet("standardOperationTheaterTestDataset.xml");
		service = Context.getPatientService();
	}

	/**
	 * @verifies save new entry if object is not null
	 * @see org.openmrs.module.operationtheater.api.db.GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldSaveNewEntryIfObjectIsNotNull() throws Exception {
		Surgery surgery = new Surgery();
		Patient patient = service.getPatient(1);
		surgery.setPatient(patient);
		Procedure procedure = new Procedure();
		procedure.setId(1);
		surgery.setProcedure(procedure);
		surgery.setSurgeryCompleted(false);

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
		Surgery surgery = new Surgery();
		int id = 1;
		surgery.setSurgeryId(id);
		Procedure procedure = new Procedure();
		procedure.setId(1);
		surgery.setProcedure(procedure);
		surgery.setSurgeryCompleted(false);

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
	public void getAll_shouldReturnAllEntriesInTheTable() {
		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));
	}

	@Test
	@Verifies(value = "should return the object with the specified uuid", method = "getByUuid")
	public void getByUuid_shouldReturnTheObjectWithTheSpecifiedUuid() throws Exception {
		String uuid = "ca352fc1-1691-11df-97a5-7038c432aab5";
		Surgery surgery = surgeryDAO.getByUuid(uuid);

		assertThat(surgery.getId(), is(1));
		assertThat(surgery.getPatient().getId(), is(1));
		//		assertThat(surgery.getCreator().getId(), is(1));
	}

	/**
	 * @verifies return all unvoided surgery entries for this patient
	 * @see SurgeryDAO#getSurgeriesByPatient(org.openmrs.Patient)
	 */
	@Test
	public void getSurgeriesByPatient_shouldReturnAllUnvoidedSurgeryEntriesForThisPatient() throws Exception {
		Patient patient = new Patient();
		patient.setId(1);
		List<Surgery> surgeryList = surgeryDAO.getSurgeriesByPatient(patient);

		assertThat(surgeryList, hasSize(1));
		assertThat(surgeryList.get(0).getPatient().getId(), is(1));
		assertThat(surgeryList.get(0).getSurgeryId(), is(1));
	}

	/**
	 * @verifies return all surgeries in the db that have not yet been performed
	 * @see SurgeryDAO#getAllUncompletedSurgeries()
	 */
	@Test
	public void getAllUncompletedSurgeries_shouldReturnAllSurgeriesInTheDbThatHaveNotYetBeenPerformed() throws Exception {

		List<Surgery> surgeryList = surgeryDAO.getAllUncompletedSurgeries();

		assertThat(surgeryList, hasSize(3));
		assertThat(surgeryList.get(0).getId(), is(2));
		assertThat(surgeryList.get(1).getId(), is(3));
		assertThat(surgeryList.get(2).getId(), is(4));
	}
}
