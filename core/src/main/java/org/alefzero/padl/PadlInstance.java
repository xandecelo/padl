package org.alefzero.padl;

import java.io.IOException;
import java.nio.file.Path;

import org.alefzero.padl.config.PadlConfig;
import org.alefzero.padl.config.PadlServiceManager;
import org.alefzero.padl.sources.PadlSourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PadlInstance {
	protected static final Logger logger = LogManager.getLogger();

	private PadlServiceManager manager;
	private PadlConfig config;

	public void loadConfiguration(Path configurationFile) throws IOException {
		this.manager = new PadlServiceManager(configurationFile);
		this.config = manager.getConfig();
	}

	public void checkYAML() {
		// TODO Auto-generated method stub
	}

	public void getGlobalOSVariables() {
		// prints to stdout for use with bash
		System.out.println("LDAP_ADM_PASSWORD=" + config.getAdminPassword());
	}

	public void checkConnectivity() {
		// TODO Auto-generated method stub

	}

	public void getSourceOSScriptList() {
		// TODO Auto-generated method stub

	}

	public void getLDIFSetupConfiguration() {
		// prints to stdout for use with bash
		System.out.println(config.getLDIFSetupConfiguration());
	}

	public void getSourceOSEnv(String sourceType) {
		// TODO Auto-generated method stub

	}

	public void runSyncProcess() {
		for (var sourceConfig : config.getSourcesInConfigurationOrder()) {
			PadlSourceService source = manager.getSourceById(sourceConfig.getId());
			source.sync();
		}
	}

}
