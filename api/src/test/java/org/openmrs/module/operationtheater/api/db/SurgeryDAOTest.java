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

import java.text.SimpleDateFormat;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link ProcedureDAO}.
 */
public class SurgeryDAOTest extends BaseModuleContextSensitiveTest {

	@Autowired
	SurgeryDAO surgeryDAO;

	private PatientService service;

	private static int TOTAL_SURGERIES = 1;

	@Before
	public void setUp() throws Exception{
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

		surgeryDAO.saveOrUpdate(surgery);

		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES +1));

		Surgery actualSurgery = surgeryList.get(TOTAL_SURGERIES);
		assertThat(actualSurgery.getSurgeryId(), greaterThan(0));
		assertEquals(patient, actualSurgery.getPatient());
	}

	/**
	 * @verifies not save object if it is null
	 * @see org.openmrs.module.operationtheater.api.db.GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldNotSaveObjectIfItIsNull() throws Exception {
		try{
			surgeryDAO.saveOrUpdate(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e){}

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

		Patient patient = service.getPatient(2);
		surgery.setPatient(patient);

		surgeryDAO.saveOrUpdate(surgery);

		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));

		Surgery actualSurgery = surgeryList.get(id-1);
		assertEquals(surgery.getSurgeryId(), actualSurgery.getSurgeryId());
		assertEquals(patient, actualSurgery.getPatient());
	}

	@Test
	@Verifies(value="should return all entries in the table", method = "getAll")
	public void getAll_shouldReturnAllEntriesInTheTable(){
		List<Surgery> surgeryList = surgeryDAO.getAll();
		assertThat(surgeryList, hasSize(TOTAL_SURGERIES));
	}

}
