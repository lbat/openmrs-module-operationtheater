package org.openmrs.module.operationtheater.api.db.hibernate;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.operationtheater.api.db.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * It is a default implementation of {@link org.openmrs.module.operationtheater.api.db.GenericDAO}.
 */
public class HibernateGenericDAO<T> implements GenericDAO<T> {

	@Autowired
	protected SessionFactory sessionFactory;

	protected Class<T> mappedClass;

	/**
	 * Marked private because you *must* provide the class at runtime when instantiating one of
	 * these, using the next constructor
	 */
	@SuppressWarnings("unused")
	private HibernateGenericDAO() {
	}

	/**
	 * You must call this before using any of the data access methods, since it's not actually
	 * possible to write them all with compile-time class information.
	 *
	 * @param mappedClass
	 */
	protected HibernateGenericDAO(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public T getById(Integer id) {
		return (T) sessionFactory.getCurrentSession().get(mappedClass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public T getByUuid(String uuid) {
		return (T) sessionFactory.getCurrentSession()
				.createQuery("from " + mappedClass.getSimpleName() + " at where at.uuid = :uuid").setString("uuid", uuid)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll() {
		return (List<T>) sessionFactory.getCurrentSession().createCriteria(mappedClass).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		return (List<T>) (includeRetired ?
				criteria.list() :
				criteria.add(Restrictions.eq("retired", includeRetired)).list());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAllData(boolean includeVoided) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		return (List<T>) (includeVoided ? criteria.list() : criteria.add(Restrictions.eq("voided", includeVoided)).list());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll(String fuzzySearchPhrase) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.ilike("name", fuzzySearchPhrase, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}

	@Override
	@Transactional
	public T saveOrUpdate(T object) {
		sessionFactory.getCurrentSession().saveOrUpdate(object);
		return object;
	}

	@Override
	@Transactional
	public T update(T object) {
		sessionFactory.getCurrentSession().update(object);
		return object;
	}

	@Override
	@Transactional
	public void delete(T object) {
		sessionFactory.getCurrentSession().delete(object);
	}
}
