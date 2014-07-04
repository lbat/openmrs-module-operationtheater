package org.openmrs.module.operationtheater;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.Patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Defines a Surgery in the system.
 */
@Entity
@Table(name = "surgery")
public class Surgery extends BaseOpenmrsDataJPA {

	@Id
	@GeneratedValue
	@Column(name = "surgery_id")
	private Integer surgeryId;

	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@Column(name = "surgery_completed", columnDefinition = "boolean default false", nullable = false)
	private Boolean surgeryCompleted = false;

	/**
	 * Planned begin date of this surgery
	 */
	@Column(name = "date_planned_begin")
	@Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
	private DateTime datePlannedBegin;

	/**
	 * Planned finish date of this surgery
	 */
	@Column(name = "date_planned_finish")
	@Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
	private DateTime datePlannedFinish;

	@ManyToOne
	@JoinColumn(name = "planned_location_id")
	private Location plannedLocation;

	public int getSurgeryId() {
		return surgeryId;
	}

	public void setSurgeryId(int surgeryId) {
		this.surgeryId = surgeryId;
	}

	@Override
	public Integer getId() {
		return getSurgeryId();
	}

	@Override
	public void setId(Integer integer) {
		setSurgeryId(integer);
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Boolean getSurgeryCompleted() {
		return surgeryCompleted;
	}

	public void setSurgeryCompleted(Boolean surgeryCompleted) {
		this.surgeryCompleted = surgeryCompleted;
	}

	public DateTime getDatePlannedBegin() {
		return datePlannedBegin;
	}

	public void setDatePlannedBegin(DateTime datePlannedBegin) {
		this.datePlannedBegin = datePlannedBegin;
	}

	public DateTime getDatePlannedFinish() {
		return datePlannedFinish;
	}

	public void setDatePlannedFinish(DateTime datePlannedFinish) {
		this.datePlannedFinish = datePlannedFinish;
	}

	public Location getPlannedLocation() {
		return plannedLocation;
	}

	public void setPlannedLocation(Location plannedLocation) {
		this.plannedLocation = plannedLocation;
	}
}
