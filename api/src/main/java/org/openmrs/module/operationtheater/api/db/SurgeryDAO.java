package org.openmrs.module.operationtheater.api.db;

import org.openmrs.Patient;
import org.openmrs.module.operationtheater.Surgery;

import java.util.List;

/**
 * DAO for {@link org.openmrs.module.operationtheater.Surgery}.
 */
public interface SurgeryDAO extends GenericDAO<Surgery> {

	/**
	 * return all unvoided surgery entries for this patient
	 *
	 * @param patient
	 * @return all unvoided surgery entries for this patient
	 * @should return all unvoided surgery entries for this patient
	 */
	public List<Surgery> getSurgeriesByPatient(Patient patient);

}
