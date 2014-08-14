package org.openmrs.module.operationtheater.scheduler.solver;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.openmrs.module.operationtheater.scheduler.domain.TimetableEntry;

import java.util.Comparator;

/**
 * This comparator sorts a list of TimeTableEntries by<br />
 * * location ascending
 * * if it has been started descending (only if instanceof PlannedSurgery)
 * * scheduled start time ascending
 */
public class TimetableEntryComparator implements Comparator<TimetableEntry> {

	/**
	 * @param left
	 * @param right
	 * @return
	 * @should sorting PlannedSurgeries based on this Comparator should sort by location asc isStarted desc and start time asc
	 */
	@Override
	public int compare(TimetableEntry left, TimetableEntry right) {
		String leftLocationUuid = left.getLocation() != null ? left.getLocation().getUuid() : "aaaaa";
		String rightLocationUuid = right.getLocation() != null ? right.getLocation().getUuid() : "aaaaa";
		long leftMillis = left.getStart() != null ? left.getStart().getMillis() : Long.MAX_VALUE;
		long rightMillis = right.getStart() != null ? right.getStart().getMillis() : Long.MAX_VALUE;

		//ensure that a started plannedSurgery is always the first element for a location
		int leftStarted = 0;
		int rightStarted = 0;
		if (left instanceof PlannedSurgery && right instanceof PlannedSurgery) {
			if (left instanceof PlannedSurgery && ((PlannedSurgery) left).getSurgery().getDateStarted() != null) {
				leftStarted = -1;
			}
			if (right instanceof PlannedSurgery && ((PlannedSurgery) right).getSurgery().getDateStarted() != null) {
				rightStarted = -1;
			}
		}

		return new CompareToBuilder()
				.append(leftLocationUuid, rightLocationUuid)
				.append(leftStarted, rightStarted)
				.append(leftMillis, rightMillis)
				.toComparison();
	}
}
