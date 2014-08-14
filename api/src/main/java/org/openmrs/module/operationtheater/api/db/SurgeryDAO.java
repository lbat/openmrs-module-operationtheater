package org.openmrs.module.operationtheater.api.db;

import org.joda.time.DateTime;
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

	/**
	 * returns all surgeries in the db that haven't yet been performed
	 *
	 * @return
	 * @should return all unvoided surgeries in the db that have not yet been performed
	 */
	public List<Surgery> getAllUncompletedSurgeries();

	/**
	 * returns all surgeries that are scheduled between the given timeframe
	 *
	 * @param from
	 * @param to
	 * @return
	 * @should return all unvoided surgeries that are scheduled between from and to date
	 */
	public List<Surgery> getScheduledSurgeries(DateTime from, DateTime to);

	/**
	 * returns all unvoided surgeries that have been started, but have not been finished yet
	 *
	 * @param dateTime
	 * @return
	 * @should return all unvoided surgeries that are started before dateTime but are not finished
	 * @should return empty list if dateTime is null
	 */
	public List<Surgery> getAllOngoingSurgeries(DateTime dateTime);
}
