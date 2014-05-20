package org.openmrs.module.operationtheater.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.module.operationtheater.api.db.hibernate.HibernateSurgeryDAO;
import org.openmrs.module.operationtheater.api.impl.SurgeryServiceImpl;

/**
 * Created by lukas on 19.05.14.
 */
public class SurgeryServiceTest {

	private SurgeryService service;

	private HibernateSurgeryDAO surgeryDAO;

	@Before
	public void setUp(){
		service = new SurgeryServiceImpl();
		surgeryDAO = Mockito.mock(HibernateSurgeryDAO.class);
		service.setSurgeryDAO(surgeryDAO);
	}

	/**
	 * @verifies create new db entry if object is not null
	 * @see SurgeryService#createSurgery(org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void createSurgery_shouldCreateNewDbEntryIfObjectIsNotNull() throws Exception {
		Surgery surgery = new Surgery();
		service.createSurgery(surgery);
		Mockito.verify(surgeryDAO).create(surgery);
	}
}
