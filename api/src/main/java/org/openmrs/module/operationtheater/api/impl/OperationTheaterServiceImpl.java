/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.operationtheater.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.api.db.OperationTheaterDAO;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * It is a default implementation of {@link OperationTheaterService}.
 */
public class OperationTheaterServiceImpl extends BaseOpenmrsService implements OperationTheaterService {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private ProcedureDAO procedureDAO;

	@Autowired
	private SurgeryDAO surgeryDAO;

	@Resource(name="patientService")
	private PatientService patientService;

	@Override
	public void setSurgeryDAO(SurgeryDAO dao) {
		this.surgeryDAO = dao;
	}

	@Override
	public void setProcedureDAO(ProcedureDAO dao) {
		this.procedureDAO = dao;
	}

	@Override
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	@Override
	public Surgery saveSurgery(Surgery surgery) throws APIException {
		ValidateUtil.validate(surgery);
		return surgeryDAO.saveOrUpdate(surgery);
	}

	@Override
	public List<Surgery> getAllSurgeries(boolean includeVoided) {
		return surgeryDAO.getAllData(includeVoided);
	}

	@Override
	public Surgery getSurgeryByUuid(String uuid) {
		return surgeryDAO.getByUuid(uuid);
	}

	@Override
	public Surgery voidSurgery(Surgery surgery, String reason) {
		if(surgery == null)
			return null;

		return surgeryDAO.saveOrUpdate(surgery);
	}

	@Override
	public Surgery unvoidSurgery(Surgery surgery) {
		if(surgery == null)
			return null;

		return surgeryDAO.saveOrUpdate(surgery);
	}

	@Override
	public Procedure saveProcedure(Procedure procedure) throws APIException {
		ValidateUtil.validate(procedure);
		return procedureDAO.saveOrUpdate(procedure);
	}

	@Override
	public List<Procedure> getAllProcedures() throws APIException {
		return procedureDAO.getAll();
	}

}
