package org.openmrs.module.operationtheater;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

import javax.persistence.*;

/**
 * Defines a Surgery in the system.
 *
 */
@Entity
@Table(name = "surgery")
public class Surgery extends BaseOpenmrsData {

	@Id
	@GeneratedValue
	@Column(name = "surgery_id")
	private Integer surgeryId;


	@ManyToOne
	@JoinColumn(name="patient_id", nullable = false)
	private Patient patient;

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
}
