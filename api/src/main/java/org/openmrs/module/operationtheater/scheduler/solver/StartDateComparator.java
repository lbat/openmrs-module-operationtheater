package org.openmrs.module.operationtheater.scheduler.solver;

import org.joda.time.DateTime;

import java.util.Comparator;

/**
 * Created by lukas on 28.07.14.
 */
public class StartDateComparator implements Comparator<DateTime> {

	@Override
	public int compare(DateTime dateTime, DateTime dateTime2) {
		return dateTime.equals(dateTime2) ? 0 : dateTime.isBefore(dateTime2) ? -1 : 1;
	}
}
