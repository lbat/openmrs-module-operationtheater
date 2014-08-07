package org.openmrs.module.operationtheater;

import org.openmrs.Patient;
import org.openmrs.Provider;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

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

	@OneToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "scheduling_data_id")
	private SchedulingData schedulingData;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "surgical_team",
			joinColumns = { @JoinColumn(name = "surgery_id", nullable = false, updatable = false) },
			inverseJoinColumns = { @JoinColumn(name = "provider_id", nullable = false, updatable = false) })
	private Set<Provider> surgicalTeam;

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

	public SchedulingData getSchedulingData() {
		return schedulingData;
	}

	public void setSchedulingData(SchedulingData schedulingData) {
		this.schedulingData = schedulingData;
	}

	public Set<Provider> getSurgicalTeam() {
		return surgicalTeam;
	}

	public void setSurgicalTeam(Set<Provider> surgicalTeam) {
		this.surgicalTeam = surgicalTeam;
	}
}
