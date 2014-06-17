/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.operationtheater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.operationtheater.schedule.Scheduler;

import java.io.File;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class OperationTheaterModuleActivator implements ModuleActivator {

	protected Log log = LogFactory.getLog(getClass());

	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Operation Theater Module");
	}

	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Operation Theater Module refreshed");
	}

	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Operation Theater Module");
	}

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		copyNativeLibraries();
		Scheduler.demo();
		log.info("Operation Theater Module started");
	}

	/**
	 * tries to copy the lp_solve native libraries into a directory specified within the java.library.path
	 */
	private void copyNativeLibraries() {
		String libraryPath = System.getProperty("java.library.path");

		//FIXME implement better solution like specifiying the directory as module property instead of trying each of them
		Module module = ModuleFactory.getModuleById("operationtheater");
		boolean success = false;
		for (String path : libraryPath.split(File.pathSeparator)) {
			try {
				String modulePath = ModuleClassLoader.getLibCacheFolderForModule(module).getCanonicalPath() + File.separator;
				System.out.println(modulePath);

				//TODO detect platform computing platform (32bit vs 64bit version)
				File src = new File(modulePath + "lib" + File.separator + "liblpsolve-5.5-linux-x86_64.so");
				String libLpsolve = path + File.separator + "liblpsolve55.so";
				File dest = new File(libLpsolve);

				if (dest.exists()) {
					FileUtils.forceDelete(dest);
				}

				FileUtils.copyFile(src, dest);

				src = new File(modulePath + "lib" + File.separator + "liblpsolvej-5.5-linux-x86_64.so");
				String libJavaLpSolve = path + File.separator + "liblpsolve55j.so";
				dest = new File(libJavaLpSolve);

				if (dest.exists()) {
					FileUtils.forceDelete(dest);
				}

				FileUtils.copyFile(src, dest);

				success = true;
				break;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!success) {
			log.error("Could not copy native libraries into a directory within the following library path : " + libraryPath);
			throw new ModuleException(
					"Could not copy native libraries into a directory within the following library path : " + libraryPath);
		}
	}

	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Operation Theater Module");
	}

	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Operation Theater Module stopped");
	}

}
