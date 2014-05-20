package org.openmrs.module.operationtheater.api;

import org.openmrs.api.APIException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;

/**
 * Created by lukas on 19.05.14.
 */
public interface SurgeryService {

	/**
	 * Sets the DAO for this service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param dao DAO for this service
	 */
	public void setSurgeryDAO(SurgeryDAO dao);

	/**
	 * Sets the DAO for the Patient service. This is done by DI and Spring. See the
	 * applicationContext-service.xml definition file.
	 *
	 * @param dao DAO for this service
	 */
	public void setPatientDAO(PatientDAO dao);

	/**
	 * Creates Surgery in the database
	 *
	 * @param surgery surgery to be created
	 * @throws APIException
	 * @should create new db entry if object is not null
	 */
	public void createSurgery(Surgery surgery) throws APIException;
}
