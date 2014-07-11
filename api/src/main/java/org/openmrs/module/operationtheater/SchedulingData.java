package org.openmrs.module.operationtheater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.openmrs.Location;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Defines scheduling data in the system.
 */
@Entity
@Table(name = "scheduling_data")
public class SchedulingData extends BaseOpenmrsDataJPA {

	private static final Log log = LogFactory.getLog(SchedulingData.class);

	@Id
	@GeneratedValue
	@Column(name = "scheduling_data_id")
	private Integer schedulingDataId;

	/**
	 * date time when the corresponding surgery is scheduled
	 */
	@Column(name = "start")
	@Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
	private DateTime start;

	/**
	 * date time when the corresponding surgery is scheduled
	 */
	@Column(name = "end")
	@Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
	private DateTime end;

	/**
	 * tells the scheduler that this surgery MUST be performed on the date
	 * specified in field: start
	 */
	@Basic
	@Column(name = "date_locked", columnDefinition = "boolean default false", nullable = false)
	private Boolean dateLocked = false;

	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location location;

	public Integer getSchedulingDataId() {
		return schedulingDataId;
	}

	public void setSchedulingDataId(Integer schedulingDataId) {
		this.schedulingDataId = schedulingDataId;
	}

	@Override
	public Integer getId() {
		return getSchedulingDataId();
	}

	@Override
	public void setId(Integer id) {
		setSchedulingDataId(id);
	}

	public DateTime getStart() {
		return start;
	}

	public void setStart(DateTime start) {
		this.start = start;
	}

	public Boolean getDateLocked() {
		return dateLocked;
	}

	public void setDateLocked(Boolean dateLocked) {
		this.dateLocked = dateLocked;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public DateTime getEnd() {
		return end;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}
}
