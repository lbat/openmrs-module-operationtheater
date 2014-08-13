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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.api.db.ProcedureDAO;
import org.openmrs.module.operationtheater.api.db.SurgeryDAO;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * It is a default implementation of {@link OperationTheaterService}.
 */
//@Service
public class OperationTheaterServiceImpl extends BaseOpenmrsService implements OperationTheaterService {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private ProcedureDAO procedureDAO;

	@Autowired
	private SurgeryDAO surgeryDAO;

	@Resource(name = "patientService")
	private PatientService patientService;

	@Resource(name = "appointmentService")
	private AppointmentService appointmentService;

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
	public void setAppointmentService(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
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
		if (surgery == null) {
			return null;
		}

		return surgeryDAO.saveOrUpdate(surgery);
	}

	@Override
	public Surgery unvoidSurgery(Surgery surgery) {
		if (surgery == null) {
			return null;
		}

		return surgeryDAO.saveOrUpdate(surgery);
	}

	@Override
	public List<Surgery> getSurgeriesByPatient(Patient patient) {
		if (patient == null) {
			return null;
		}
		return surgeryDAO.getSurgeriesByPatient(patient);
	}

	@Override
	public Procedure retireProcedure(Procedure procedureToRetire, String reason) {
		if (procedureToRetire == null) {
			return null;
		}

		return procedureDAO.saveOrUpdate(procedureToRetire);
	}

	@Override
	public Surgery getSurgery(Integer id) {
		if (id == null) {
			return null;
		}
		return surgeryDAO.getById(id);
	}

	@Override
	public List<Surgery> getAllUncompletedSurgeries() {
		return surgeryDAO.getAllUncompletedSurgeries();
	}

	@Override
	public List<Surgery> getScheduledSurgeries(DateTime from, DateTime to) {
		if (from == null || to == null) {
			return new ArrayList<Surgery>();
		}
		return surgeryDAO.getScheduledSurgeries(from, to);
	}

	@Override
	public Procedure getProcedureByUuid(String uuid) {
		return procedureDAO.getByUuid(uuid);
	}

	@Override
	public Procedure getProcedure(Integer id) {
		return procedureDAO.getById(id);
	}

	@Override
	public Procedure saveProcedure(Procedure procedure) throws APIException {
		ValidateUtil.validate(procedure);
		return procedureDAO.saveOrUpdate(procedure);
	}

	@Override
	public List<Procedure> getAllProcedures(boolean includeRetired) throws APIException {
		return procedureDAO.getAll(includeRetired);
	}

	@Override
	public Interval getLocationAvailableTime(Location location, DateTime date) {
		Date date1 = date.toDate();
		List<AppointmentBlock> blocks = appointmentService.getAppointmentBlocks(date.withTime(0, 0, 0, 0).toDate(),
				date.withTime(0, 0, 0, 0).plusDays(1).toDate(), location.getId() + ",", null, null);
		//		List<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();

		if (blocks.size() == 1) {
			return new Interval(new DateTime(blocks.get(0).getStartDate()), new DateTime(blocks.get(0).getEndDate()));
		} else if (blocks.size() > 1) {
			throw new APIException("There shouldn't be multiple appointment blocks per location and date");
		}

		DateTimeFormatter timeFormatter = OTMetadata.AVAILABLE_TIME_FORMATTER;
		DateTime availableStart = null;
		DateTime availableEnd = null;
		for (LocationAttribute attribute : location.getAttributes()) {
			if (attribute.getAttributeType().getUuid().equals(OTMetadata.DEFAULT_AVAILABLE_TIME_BEGIN_UUID)) {
				LocalTime beginTime = LocalTime.parse((String) attribute.getValue(), timeFormatter);
				availableStart = date.withTime(beginTime.getHourOfDay(), beginTime.getMinuteOfHour(), 0, 0);
			} else if (attribute.getAttributeType().getUuid().equals(OTMetadata.DEFAULT_AVAILABLE_TIME_END_UUID)) {
				LocalTime endTime = LocalTime.parse((String) attribute.getValue(), timeFormatter);
				availableEnd = date.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(), 0, 0);
			}
		}

		if (availableStart != null && availableEnd != null) {
			return new Interval(availableStart, availableEnd);
		}

		throw new APIException("Available times not defined. please make sure that the attributes " +
				"'default available time begin' and 'default available time end' for the location " + location.getName()
				+ " are defined");

	}

}
