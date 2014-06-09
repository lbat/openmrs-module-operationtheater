package org.openmrs.module.operationtheater.page.controller;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 */
public class SchedulingPageController {

	/**
	 * This page is built to be shared across multiple apps. To use it, you must pass an "app"
	 * request parameter, which must be the id of an existing app that is an instance of
	 * coreapps.template.findPatient
	 *
	 * @param model
	 * @param sessionContext
	 */
	public void get(PageModel model, UiSessionContext sessionContext, UiUtils ui) {
		model.addAttribute("dateFormatter", new SimpleDateFormat("dd-MMM-yyy", Context.getLocale()));

		LocationService locationService = Context.getLocationService();
		LocationTag tag = locationService.getLocationTagByUuid("af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2");
		List<Location> locations = locationService.getLocationsByTag(tag);

		model.addAttribute("resources", locations);

		//		if (app.getConfig().get("showLastViewedPatients").getBooleanValue()) {
		//			List<Patient> patients = GeneralUtils.getLastViewedPatients(sessionContext.getCurrentUser());
		//			model.addAttribute("lastViewedPatients", patients);
		//		}
		//		model.addAttribute("showLastViewedPatients", app.getConfig().get("showLastViewedPatients").getBooleanValue());
	}

}
