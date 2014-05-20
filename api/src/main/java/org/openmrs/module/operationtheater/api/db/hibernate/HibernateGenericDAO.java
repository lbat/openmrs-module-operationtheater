package org.openmrs.module.operationtheater.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.openmrs.module.operationtheater.api.db.GenericDAO;

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
public class HibernateGenericDAO<T> implements GenericDAO<T> {

	protected final static Log log = LogFactory.getLog(HibernateGenericDAO.class);

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	private Class<T> clazz;

	public HibernateGenericDAO(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public T get(long id) {
		return (T) sessionFactory.getCurrentSession().get(clazz, id);
	}

	@Override
	public List<T> getAll() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(clazz, "r");

		//if (!includeRetired) {
		//	criteria.add(Expression.eq("retired", false));
		//}

		criteria.addOrder(Order.asc("sortWeight"));

		return criteria.list();
	}

	@Override
	public void create(T t) {
		sessionFactory.getCurrentSession().save(t);
	}

	@Override
	public void update(T t) {
		sessionFactory.getCurrentSession().update(t);
	}

	@Override
	public void delete(T t) {
		sessionFactory.getCurrentSession().delete(t);
	}

	@Override
	public void deleteById(long id) {
		sessionFactory.getCurrentSession().delete(get(id));
	}
}
