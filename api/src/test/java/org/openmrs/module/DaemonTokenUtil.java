package org.openmrs.module;

import org.openmrs.module.operationtheater.OperationTheaterModuleActivator;

public class DaemonTokenUtil {

	/**
	 * method is used to obtain a DaemonToken during (integration) tests
	 */
	public static void passDaemonTokenToModule() {
		Module module = new Module("name");
		module.setModuleActivator(new OperationTheaterModuleActivator());
		ModuleFactory.passDaemonToken(module);
	}
}
