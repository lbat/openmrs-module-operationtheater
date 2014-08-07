package org.openmrs.module.operationtheater.scheduler.solver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openmrs.module.operationtheater.Surgery;

/**
 * Calculated during initialization, not modified during score calculation.
 * A cached problem fact is a problem fact that doesn't exist in the real domain model, but is
 * calculated before the Solver really starts solving. This can lead to simpler and faster score constraints.
 */
public class SurgeryConflict {

	/**
	 * number of persons that are in conflict between the two PlannedSurgeries
	 */
	private final int numberOfPersons;

	private Surgery left;

	private Surgery right;

	public SurgeryConflict(Surgery left, Surgery right, int numberOfPersons) {
		this.left = left;
		this.right = right;
		this.numberOfPersons = numberOfPersons;
	}

	public Surgery getLeft() {
		return left;
	}

	public void setLeft(Surgery left) {
		this.left = left;
	}

	public Surgery getRight() {
		return right;
	}

	public void setRight(Surgery right) {
		this.right = right;
	}

	public int getNumberOfPersons() {
		return numberOfPersons;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof SurgeryConflict) {
			SurgeryConflict other = (SurgeryConflict) o;
			return new EqualsBuilder()
					.append(left, other.left)
					.append(right, other.right)
					.append(numberOfPersons, other.numberOfPersons)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(left)
				.append(right)
				.append(numberOfPersons)
				.toHashCode();
	}

	@Override
	public String toString() {
		return "PlannedSurgeryConflict{" +
				"left=" + left +
				", right=" + right +
				", numberOfPersons=" + numberOfPersons +
				'}';
	}
}
