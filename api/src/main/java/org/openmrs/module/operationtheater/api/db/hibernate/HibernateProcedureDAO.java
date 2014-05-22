package org.openmrs.module.operationtheater.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * It is a default implementation of {@link org.openmrs.module.operationtheater.api.db.ProcedureDAO}.
 */
//@Qualifier("procedureDAO")
@Repository
public class HibernateProcedureDAO extends HibernateGenericDAO<Procedure> implements ProcedureDAO {
	protected final Log log = LogFactory.getLog(this.getClass());

	public HibernateProcedureDAO() {
		super(Procedure.class);
	}
}
