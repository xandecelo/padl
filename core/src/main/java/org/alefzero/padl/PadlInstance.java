package org.alefzero.padl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.alefzero.padl.config.PadlConfig;
import org.alefzero.padl.config.PadlServiceManager;
import org.alefzero.padl.sources.PadlSourceConfiguration;
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
		System.out.println(String.join("\n",
				config.getSourcesInConfigurationOrder().stream().filter(PadlSourceConfiguration::hasOSPreScript)
						.map(PadlSourceConfiguration::getOSRequirementScript).collect(Collectors.toList())));
	}

	public void getLDIFSetupConfiguration() {
		// prints to stdout for use with bash
		System.out.println(config.getLDIFSetupConfiguration());
	}

	public void getSourceParameterOSEnv(String sourceId) {
		logger.trace(".getSourceParameterOSEnv [sourceId={}]", sourceId);
		System.out.print(manager.getSourceById(sourceId).getConfig().getOSEnv());

	}

	public void sync() {
		logger.debug("Syncing process is up and running.");
		for (var sourceConfig : config.getSourcesInConfigurationOrder()) {
			logger.trace("Running sync for {}", sourceConfig.getId());
			PadlSourceService source = manager.getSourceById(sourceConfig.getId());
			source.sync();
			logger.trace("Sync for {}", sourceConfig.getId());

		}
	}

	public void prepareSync() {
		logger.debug("Preparing sync...");

		for (var sourceConfig : config.getSourcesInConfigurationOrder()) {
			PadlSourceService source = manager.getSourceById(sourceConfig.getId());
			source.prepare();
		}
	}

}
