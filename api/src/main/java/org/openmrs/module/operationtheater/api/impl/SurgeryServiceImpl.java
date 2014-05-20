package org.openmrs.module.operationtheater.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.SurgeryService;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;

/**
 * Created by lukas on 19.05.14.
 */
public class SurgeryServiceImpl implements SurgeryService {

	private SurgeryDAO surgeryDAO;

	private PatientDAO patientDao;

	@Override
	public void setSurgeryDAO(SurgeryDAO dao) {
		this.surgeryDAO = dao;
	}

	@Override
	public void setPatientDAO(PatientDAO dao) {
		this.patientDao = dao;
	}

	@Override
	public void createSurgery(Surgery surgery) throws APIException {
		surgeryDAO.create(surgery);
	}
}
