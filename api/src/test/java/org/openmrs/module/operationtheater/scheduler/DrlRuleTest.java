package org.openmrs.module.operationtheater.scheduler;

import org.drools.compiler.compiler.DroolsError;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.PackageBuilderErrors;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatefulSession;
import org.drools.core.base.RuleNameStartsWithAgendaFilter;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.scheduler.domain.PlannedSurgery;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScoreHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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

	@Test
	public void testRule_overlappingSurgeriesInSameOperationTheater() {
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

		session.insert(ps1);
		session.insert(ps2);

		session.fireAllRules(new RuleNameStartsWithAgendaFilter("overlappingSurgeriesInSameOperationTheater"));
		HardSoftScoreHolder scoreHolder = (HardSoftScoreHolder) session.getGlobal("scoreHolder");

		//verify
		assertThat(scoreHolder.getHardScore(), is(-1));
		assertThat(scoreHolder.getSoftScore(), is(0));
	}

	@Test
	public void testRule_firstComeFirstServed() {

		DateTime now = new DateTime();
		int waitingTime = 5;
		DateTime surgeryCreated = now.minusDays(waitingTime);

		PlannedSurgery plannedSurgery = new PlannedSurgery();
		Surgery surgery = new Surgery();
		surgery.setDateCreated(surgeryCreated.toDate());
		plannedSurgery.setSurgery(surgery);
		plannedSurgery.setStart(now, false);

		session.insert(plannedSurgery);
		session.fireAllRules(new RuleNameStartsWithAgendaFilter("firstComeFirstServed"));
		HardSoftScoreHolder scoreHolder = (HardSoftScoreHolder) session.getGlobal("scoreHolder");

		assertThat(scoreHolder.getHardScore(), is(0));
		assertThat(scoreHolder.getSoftScore(), is(-1 * waitingTime));
	}
}
