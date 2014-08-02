package org.openmrs.module.operationtheater.scheduler.domain;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Tests {@link Anchor}
 */
public class AnchorTest {

	/**
	 * @verifies return maxChainLengthInMinutes if no timetable element is attached
	 * @see Anchor#getRemainingTime()
	 */
	@Test
	public void getRemainingTime_shouldReturnMaxChainLengthInMinutesIfNoTimetableElementIsAttached() throws Exception {
		Anchor anchor = new Anchor(null, null);
		int availableMinutes = 69;
		anchor.setMaxChainLengthInMinutes(availableMinutes);

		//call method under test
		int result = anchor.getRemainingTime();

		//verify
		assertThat(result, is(availableMinutes));
	}

	/**
	 * @verifies return maxChainLengthInMinutes minus length of successor chain
	 * @see Anchor#getRemainingTime()
	 */
	@Test
	public void getRemainingTime_shouldReturnMaxChainLengthInMinutesMinusLengthOfSuccessorChain() throws Exception {
		PlannedSurgery psMock = Mockito.mock(PlannedSurgery.class);
		Integer duration = 23;
		when(psMock.getChainLengthInMinutes()).thenReturn(duration);

		Anchor anchor = new Anchor(null, null);
		int availableMinutes = 69;
		anchor.setMaxChainLengthInMinutes(availableMinutes);
		anchor.setNextTimetableEntry(psMock);

		//call method under test
		int result = anchor.getRemainingTime();

		//verify
		assertThat(result, is(availableMinutes - duration));
	}
}
