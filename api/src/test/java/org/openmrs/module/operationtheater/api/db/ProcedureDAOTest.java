package org.openmrs.module.operationtheater.api.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link org.openmrs.module.operationtheater.api.db.ProcedureDAO}.
 */
public class ProcedureDAOTest extends BaseModuleContextSensitiveTest {

	@Autowired
	ProcedureDAO procedureDAO;

	private static int TOTAL_PROCEDURES = 2;

	@Before
	public void setUp() throws Exception{
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
		procedure.setInterventionDuration(new SimpleDateFormat("HH:mm").parse("01:15"));
		procedure.setOtPreparationDuration(new SimpleDateFormat("HH:mm").parse("00:30"));
		procedure.setInpatientStay(4);

		procedureDAO.saveOrUpdate(procedure);

		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES+1));

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
		try{
			procedureDAO.saveOrUpdate(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e){}

		List<Procedure> procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES));
	}

	/**
	 * @verifies update object if it is not null and id already in the db
	 * @see GenericDAO#saveOrUpdate(T)
	 */
	@Test
	public void saveOrUpdate_shouldUpdateObjectIfItIsNotNullAndIdAlreadyInTheDb() throws Exception {
		saveOrUpdate_shouldSaveNewEntryIfObjectIsNotNull();

		List<Procedure> procedureList = procedureDAO.getAll();
		Procedure procedure = procedureList.get(TOTAL_PROCEDURES);

		procedure.setName("Another Test procedure");
		procedure.setDescription("Another Test procedure description");
		procedure.setInterventionDuration(new SimpleDateFormat("HH:mm").parse("11:15"));
		procedure.setOtPreparationDuration(new SimpleDateFormat("HH:mm").parse("01:30"));
		procedure.setInpatientStay(10);

		procedureDAO.saveOrUpdate(procedure);

		procedureList = procedureDAO.getAll();
		assertThat(procedureList, hasSize(TOTAL_PROCEDURES+1));

		Procedure actualProcedure = procedureList.get(TOTAL_PROCEDURES);
		assertEquals(procedure.getProcedureId(), actualProcedure.getProcedureId());
		assertEquals(procedure.getName(), actualProcedure.getName());
		assertEquals(procedure.getDescription(), actualProcedure.getDescription());
		assertEquals(procedure.getInterventionDuration(), actualProcedure.getInterventionDuration());
		assertEquals(procedure.getOtPreparationDuration(), actualProcedure.getOtPreparationDuration());
		assertEquals(procedure.getInpatientStay(), actualProcedure.getInpatientStay());
	}
}
