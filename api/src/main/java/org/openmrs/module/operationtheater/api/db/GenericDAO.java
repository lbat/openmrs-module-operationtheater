package org.openmrs.module.operationtheater.api.db;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hibernate specific GenericDAO database methods.
 */
@Repository
public interface GenericDAO<T> {

	T getById(Integer id);

	T getByUuid(String uuid);

	/**
	 * returns all entries in the table
	 *
	 * @return
	 * @should return all entries in the table
	 */
	List<T> getAll();

	List<T> getAll(boolean includeRetired);

	List<T> getAllData(boolean includeVoided);

	List<T> getAll(String fuzzySearchPhrase);

	/**
	 * save or update the object in the database
	 *
	 * @param object
	 * @return
	 * @should save new entry if object is not null
	 * @should not save object if it is null
	 * @should  update object if it is not null and id already in the db
	 */
	T saveOrUpdate(T object);

	T update(T object);

	void delete(T object);
}
