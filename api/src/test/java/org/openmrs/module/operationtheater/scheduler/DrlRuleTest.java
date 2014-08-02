package org.openmrs.module.operationtheater.scheduler;

import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.PackageBuilderErrors;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatefulSession;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.domain.Anchor;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScoreHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DrlRuleTest {

	StatefulSession session;

	@Before
	public void setUp() throws IOException, DroolsParserException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("scheduler/scoreRules.drl");

		PackageBuilder builder = new PackageBuilder();

		builder.addPackageFromDrl(new InputStreamReader(stream));

		PackageBuilderErrors errors = builder.getErrors();
		assertThat(errors, hasSize(0));

		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(builder.getPackage());

		session = ruleBase.newStatefulSession(false);
		session.setGlobal("scoreHolder", new HardSoftScoreHolder(true));
	}

	@After
	public void tearDown() {
		session.dispose();
	}

	@Test
	public void testRule_overlappingSurgeriesInSameOperationTheater() {
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getLocationAvailableTime(any(Location.class), any(DateTime.class))).thenReturn(null);

		Location location = new Location();

		PlannedSurgery ps1 = new PlannedSurgery();
		PlannedSurgery ps2 = new PlannedSurgery();

		ps1.setLocation(location);
		ps2.setLocation(location);

		DateTime start1 = new DateTime();
		DateTime start2 = start1.plusHours(1);

		DateTime end1 = start1.plusHours(2);
		DateTime end2 = end1.plusHours(1);

		ps1.setStart(start1, false);
		ps2.setStart(start2, false);
		ps1.setEnd(end1);
		ps2.setEnd(end2);

		//it seems that all methods are called on inserts -> we have to mock OperationTheaterService
		Whitebox.setInternalState(ps1, "otService", otService);
		Whitebox.setInternalState(ps2, "otService", otService);

		session.insert(ps1);
		session.insert(ps2);

		session.fireAllRules(new RuleNameEqualsAgendaFilter("overlappingSurgeriesInSameOperationTheater"), 1);
		HardSoftScoreHolder scoreHolder = (HardSoftScoreHolder) session.getGlobal("scoreHolder");

		//verify
		assertThat(scoreHolder.getHardScore(), is(-1));
		assertThat(scoreHolder.getSoftScore(), is(0));
	}

	@Test
	public void testRule_firstComeFirstServed() {

		DateTime now = new DateTime();
		int waitingTime = 54;
		DateTime surgeryCreated = now.minusHours(waitingTime);

		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		surgery.setDateCreated(surgeryCreated.toDate());
		plannedSurgery.setSurgery(surgery);
		plannedSurgery.setStart(now, false);
		plannedSurgery.setLocation(new Location());

		session.insert(plannedSurgery);

		//call method under test
		session.fireAllRules(new RuleNameEqualsAgendaFilter("firstComeFirstServed"));

		//verify
		HardSoftScoreHolder scoreHolder = (HardSoftScoreHolder) session.getGlobal("scoreHolder");
		assertThat(scoreHolder.getHardScore(), is(0));
		assertThat(scoreHolder.getSoftScore(), is(-1 * waitingTime));
	}

	@Test
	public void testRule_preventUnscheduledSurgeriesIfTimeLeft() {
		Anchor anchor = new Anchor(null, null);
		anchor.setMaxChainLengthInMinutes(75);

		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(60);
		procedure.setOtPreparationDuration(15);
		surgery.setProcedure(procedure);
		plannedSurgery.setSurgery(surgery);

		session.insert(plannedSurgery);
		session.insert(anchor);

		//call method under test
		session.fireAllRules(new RuleNameEqualsAgendaFilter("preventUnscheduledSurgeriesIfTimeLeft"));

		//verify
		HardSoftScoreHolder scoreHolder = (HardSoftScoreHolder) session.getGlobal("scoreHolder");
		assertThat(scoreHolder.getHardScore(), is(-1));
		assertThat(scoreHolder.getSoftScore(), is(0));
	}
}
