package org.openmrs.module.operationtheater;

import org.openmrs.Patient;

import javax.persistence.*;

/**
 * Defines a Surgery in the system. TODO javadoc
 *
 * @version 2.0
 */
@Entity
@Table(name = "surgery")
public class Surgery{ //TODO extends BaseOpenmrsObject implements OpenmrsData {

	@Id
	@GeneratedValue
	@Column(name = "surgery_id")
	private int surgeryId;

	private Patient patient;

}
