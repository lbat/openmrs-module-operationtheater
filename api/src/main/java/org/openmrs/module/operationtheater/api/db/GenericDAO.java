package org.openmrs.module.operationtheater.api.db;

import java.util.List;

/**
 * Hibernate specific GenericDAO database methods. <br/>
 * <br/>
 * This class should not be used directly. All database calls should go through the Service layer. <br/>
 * <br/>
 * Proper use: <code>
 *   PersonService ps = Context.getXYService();
 *   ps.getPeople("name", false);
 * </code>
 *
 * @see org.openmrs.api.context.Context
 */
public interface GenericDAO<T> {

	public T get(long id);

	public List<T> getAll();

	public void create(final T t);

	public void update(final T t);

	public void delete(final T t);

	public void deleteById(long id);
}
