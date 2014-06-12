package org.openmrs.module.operationtheater.api.db;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.operationtheater.Procedure;
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
 * Tests {@link org.openmrs.module.operationtheater.api.db.ProcedureDAO}.
 */
public class ProcedureDAOTest extends BaseModuleContextSensitiveTest {

	private static int TOTAL_PROCEDURES = 2;

	@Autowired
	ProcedureDAO procedureDAO;

	@Before
	public void setUp() throws Exception {
		executeDataSet("standardOperationTheaterTestDataset.xml");
	}

	/**
	 * @verifies save new entry if object is not null
	 * @see GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldSaveNewEntryIfObjectIsNotNull() throws Exception {
		Procedure procedure = new Procedure();
		procedure.setName("Test procedure");
		procedure.setDescription("Test procedure description");
		procedure.setInterventionDuration(75);
		procedure.setOtPreparationDuration(30);
		procedure.setInpatientStay(4);

		Procedure savedProcedure = procedureDAO.saveOrUpdate(procedure);
		assertThat(savedProcedure.getId(), is(TOTAL_PROCEDURES + 1));

		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES + 1));

		Procedure actualProcedure = procedureList.get(TOTAL_PROCEDURES);
		assertThat(actualProcedure.getProcedureId(), greaterThan(0));
		assertEquals(procedure.getName(), actualProcedure.getName());
		assertEquals(procedure.getDescription(), actualProcedure.getDescription());
		assertEquals(procedure.getInterventionDuration(), actualProcedure.getInterventionDuration());
		assertEquals(procedure.getOtPreparationDuration(), actualProcedure.getOtPreparationDuration());
		assertEquals(procedure.getInpatientStay(), actualProcedure.getInpatientStay());
	}

	/**
	 * @verifies not save object if it is null
	 * @see GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldNotSaveObjectIfItIsNull() throws Exception {
		try {
			procedureDAO.saveOrUpdate(null);
			fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
		}

		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES));
	}

	/**
	 * @verifies update object if it is not null and id already in the db
	 * @see GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldUpdateObjectIfItIsNotNullAndIdAlreadyInTheDb() throws Exception {
		Procedure procedure = new Procedure();
		int id = 1;
		procedure.setProcedureId(id);

		procedure.setName("Another Test procedure");
		procedure.setDescription("Another Test procedure description");
		procedure.setInterventionDuration(245);
		procedure.setOtPreparationDuration(62);
		procedure.setInpatientStay(10);

		Procedure savedProcedure = procedureDAO.saveOrUpdate(procedure);
		assertThat(savedProcedure.getId(), is(id));

		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES));

		Procedure actualProcedure = procedureList.get(id - 1);
		assertEquals(procedure.getProcedureId(), actualProcedure.getProcedureId());
		assertEquals(procedure.getName(), actualProcedure.getName());
		assertEquals(procedure.getDescription(), actualProcedure.getDescription());
		assertEquals(procedure.getInterventionDuration(), actualProcedure.getInterventionDuration());
		assertEquals(procedure.getOtPreparationDuration(), actualProcedure.getOtPreparationDuration());
		assertEquals(procedure.getInpatientStay(), actualProcedure.getInpatientStay());
	}

	@Test
	@Verifies(value = "should return all entries in the table", method = "getAll")
	public void getAll_shouldReturnAllEntriesInTheTable() {
		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES));
	}

	@Test
	@Verifies(value = "should return the object with the specified uuid", method = "getByUuid")
	public void getByUuid_shouldReturnTheObjectWithTheSpecifiedUuid() throws Exception {
		String uuid = "abcdefc1-1691-11df-97a5-7038c432aab2";
		Procedure procedure = procedureDAO.getByUuid(uuid);

		assertThat(procedure.getId(), is(2));
		assertThat(procedure.getInpatientStay(), is(8));
		assertThat(procedure.getInterventionDuration(), is(252));
		assertThat(procedure.getOtPreparationDuration(), is(46));
	}

	@Test
	/**
	 * @verifies return the object with the specified id
	 * @see GenericDAO#saveOrUpdate(T)
	 */
	public void getById_shouldReturnTheObjectWithTheSpecifiedId() throws Exception {
		Integer id = 2;
		Procedure procedure = procedureDAO.getById(id);

		assertThat(procedure.getId(), is(id));
		assertThat(procedure.getInpatientStay(), is(8));
		assertThat(procedure.getInterventionDuration(), is(252));
		assertThat(procedure.getOtPreparationDuration(), is(46));
	}

}
