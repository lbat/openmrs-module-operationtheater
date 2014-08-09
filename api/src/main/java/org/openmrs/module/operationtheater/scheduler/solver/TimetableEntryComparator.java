package org.openmrs.module.operationtheater.scheduler.solver;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;

import java.util.Comparator;

public class TimetableEntryComparator implements Comparator<TimetableEntry> {

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
		String leftLocationUuid = left.getLocation() != null ? left.getLocation().getUuid() : "aaaaa";
		String rightLocationUuid = right.getLocation() != null ? right.getLocation().getUuid() : "aaaaa";
		long leftMillis = left.getStart() != null ? left.getStart().getMillis() : Long.MAX_VALUE;
		long rightMillis = right.getStart() != null ? right.getStart().getMillis() : Long.MAX_VALUE;
		return new CompareToBuilder()
				.append(leftLocationUuid, rightLocationUuid)
				.append(leftMillis, rightMillis)
				.build();
	}
}
