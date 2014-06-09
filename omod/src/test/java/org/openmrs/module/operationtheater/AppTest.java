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
	public void testOpenSurgeryAppTemplateIsLoaded() throws Exception {
		AppTemplate template = AppTestUtil.getAppTemplate("operationtheater.template.openSurgery");
		assertThat(template.getConfigOptions().get(0).getName(), is("afterSelectedUrl"));
	}

	@Test
	public void testOpenSurgeryAppIsLoaded() throws Exception {
		AppDescriptor app = AppTestUtil.getAppDescriptor("operationtheater.openSurgery");
		assertThat(app.getOrder(), is(2));
		assertThat(app.getExtensions(), hasSize(1));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getId(), is("coreapps.activeVisitsHomepageLink"));
		assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
		assertThat(app.getExtensions().get(0).getType(), is("link"));
		assertThat(app.getExtensions().get(0).getLabel(), is("operationtheater.openSurgery.app.button.label"));
		assertThat(app.getExtensions().get(0).getUrl(),
				is("operationtheater/findSurgery.page?app=operationtheater.openSurgery"));
		assertThat(app.getExtensions().get(0).getIcon(), is("icon-file"));
		//TODO priviledge
		//assertThat(app.getExtensions().get(0).getRequiredPrivilege(), is());

		assertThat(app.getInstanceOf(), is("operationtheater.template.openSurgery"));
		assertThat(app.getTemplate().getId(), is("operationtheater.template.openSurgery"));
		String expectedUrl = app.getTemplate().getConfigOptions().get(0).getDefaultValue().getTextValue();
		assertThat(app.getConfig().get("afterSelectedUrl").getTextValue(), is(expectedUrl));
	}

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
}
