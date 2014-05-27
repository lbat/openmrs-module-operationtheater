package org.openmrs.module.operationtheater;

import org.openmrs.OpenmrsObject;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Same class as in {@link org.openmrs.BaseOpenmrsObject}, but with JPA annotations
 */
@MappedSuperclass
public abstract class BaseOpenmrsObjectJPA implements OpenmrsObject {

	@Column(nullable = false, length = 38, unique = true)
	private String uuid = UUID.randomUUID().toString();

	/**
	 * @see org.openmrs.OpenmrsObject#getUuid()
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Returns a hash code based on the <code>uuid</code> field.
	 * <p>
	 * If the <code>uuid</code> field is <code>null</code>, it delegates to
	 * {@link Object#hashCode()}.
	 *
	 * @see java.lang.Object#hashCode()
	 * @should not fail if uuid is null
	 */
	@Override
	public int hashCode() {
		if (getUuid() == null)
			return super.hashCode();
		return getUuid().hashCode();
	}

	/**
	 * Returns <code>true</code> if and only if <code>x</code> and <code>y</code> refer to the same
	 * object (<code>x == y</code> has the value <code>true</code>) or both have the same
	 * <code>uuid</code> (<code>((x.uuid != null) && x.uuid.equals(y.uuid))</code> has the value
	 * <code>true</code>).
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should return false if given obj is not instance of BaseOpenmrsObject
	 * @should return false if given obj is null
	 * @should return false if given obj has null uuid
	 * @should return false if uuid is null
	 * @should return true if objects are the same
	 * @should return true if uuids are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BaseOpenmrsObjectJPA))
			return false;
		BaseOpenmrsObjectJPA other = (BaseOpenmrsObjectJPA) obj;
		// Need to call getUuid to make sure the hibernate proxy objects return the correct uuid.
		// The private member may not be set for a hibernate proxy.
		if (getUuid() == null)
			return false;
		return getUuid().equals(other.getUuid());
	}

	/**
	 * Returns a string consisting of the name of the class of which the object is an instance and
	 * the <code>uuid</code> field surrounded by <code>[</code> and <code>]</code>. In other words,
	 * this method returns a string equal to the value of: <blockquote>
	 *
	 * <pre>
	 * getClass().getName() + '[' + uuid + ']'
	 * </pre>
	 *
	 * </blockquote>
	 * <p>
	 * If the <code>uuid</code> field is <code>null</code>, it delegates to
	 * {@link Object#toString()}
	 *
	 * @see java.lang.Object#toString()
	 * @should not fail if uuid is null
	 */
	@Override
	public String toString() {
		if (getUuid() != null) {
			return getClass().getName() + "[" + getUuid() + "]";
		} else {
			return super.toString();
		}
	}
}
