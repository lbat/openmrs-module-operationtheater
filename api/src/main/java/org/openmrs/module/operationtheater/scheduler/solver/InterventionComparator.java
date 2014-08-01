package org.openmrs.module.operationtheater.scheduler.solver;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;

import java.util.Comparator;

/**
 * Created by lukas on 30.07.14.
 */
public class InterventionComparator implements Comparator<TimetableEntry> {

	/**
	 * @param left
	 * @param right
	 * @return
	 * @should return 0 if location and start attributes are equal
	 * @should return a negative number if left.location is smaller than right.location
	 * @should return a negative number if locations are the same and left.start smaller right.start
	 * @should return a positive number if left.location is greater than right.location
	 * @should return a positive number if locations are the same and left.start greater right.start
	 */
	@Override
	public int compare(TimetableEntry left, TimetableEntry right) {
		return new CompareToBuilder()
				.append(left.getLocation().getUuid(), right.getLocation().getUuid())
				.append(left.getStart().getMillis(), right.getStart().getMillis())
				.build();
	}
}
