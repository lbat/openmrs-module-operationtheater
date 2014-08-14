package org.openmrs.module.operationtheater;

import org.junit.Test;
import org.openmrs.module.appframework.AppTestUtil;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.AppTemplate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lukas on 19.05.14.
 */
public class AppTest {

	@Test
	public void testSchedulingAppIsLoaded() throws Exception {
		AppDescriptor app = AppTestUtil.getAppDescriptor("operationtheater.scheduling");
		assertThat(app.getOrder(), is(2));
		assertThat(app.getExtensions(), hasSize(1));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getId(), is("coreapps.activeVisitsHomepageLink"));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getType(), is("link"));
		assertThat(app.getExtensions().get(0).getLabel(), is("operationtheater.scheduling.app.button.label"));
		assertThat(app.getExtensions().get(0).getUrl(), is("operationtheater/scheduling.page"));
		assertThat(app.getExtensions().get(0).getIcon(), is("icon-calendar"));
		//TODO priviledge
		//assertThat(app.getExtensions().get(0).getRequiredPrivilege(), is());
	}

	@Test
	public void testManageProcedureAppIsLoaded() throws Exception {
		AppDescriptor app = AppTestUtil.getAppDescriptor("operationtheater.manageProcedures");
		assertThat(app.getOrder(), is(2));
		assertThat(app.getExtensions(), hasSize(1));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getId(), is("operationtheater.manageProceduresHomepageLink"));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getType(), is("link"));
		assertThat(app.getExtensions().get(0).getLabel(), is("operationtheater.manageprocedures.app.button.label"));
		assertThat(app.getExtensions().get(0).getUrl(), is("operationtheater/manageProcedures.page"));
		assertThat(app.getExtensions().get(0).getIcon(), is("icon-edit"));
		//TODO priviledge
		//assertThat(app.getExtensions().get(0).getRequiredPrivilege(), is());
	}

	@Test
	public void testPatientsSurgeriesAppIsLoaded() throws Exception {
		AppDescriptor app = AppTestUtil.getAppDescriptor("operationtheater.patientsSurgeries");
		assertThat(app.getOrder(), is(2));
		assertThat(app.getExtensions(), hasSize(1));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("patientDashboard.overallActions"));
		assertThat(app.getExtensions().get(0).getId(), is("operationtheater.patientsSurgeriesPatientDashboardLink"));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("patientDashboard.overallActions"));
		assertThat(app.getExtensions().get(0).getType(), is("link"));
		assertThat(app.getExtensions().get(0).getLabel(), is("operationtheater.patientssurgeries.app.button.label"));
		assertThat(app.getExtensions().get(0).getUrl(),
				is("operationtheater/patientsSurgeries.page?patientId={{patientId}}"));
		assertThat(app.getExtensions().get(0).getIcon(), is("icon-folder-open"));
	}
}
