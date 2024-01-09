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

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;

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

		if (this.instanceDontHaveOrganization()) {
			this.createDefaultOrganization();
		}

		for (var sourceConfig : config.getSourcesInConfigurationOrder()) {
			PadlSourceService source = manager.getSourceById(sourceConfig.getId());
			source.prepare();
		}
	}

	private void createDefaultOrganization() {
		try (LDAPConnection conn = new LDAPConnection("localhost", 389, "cn=admin," + config.getSuffix(),
				config.getAdminPassword())) {
			Entry entry = new Entry(config.getLDIFForSuffixOrganization().split("\n"));
			logger.debug("Creating base entry for suffix {} with {}.", config.getSuffix(), entry);
			LDAPResult result = conn.add(entry);
			logger.debug("Operation result: {} ", result);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private boolean instanceDontHaveOrganization() {
		boolean result = true;
		logger.debug("Looking for a configured organization for suffix {}.", config.getSuffix());
		try (LDAPConnection conn = new LDAPConnection("localhost", 389, "cn=admin," + config.getSuffix(),
				config.getAdminPassword())) {
			var entry = conn.getEntry(config.getSuffix());
			logger.debug("Look at was found: {}", entry);
			result = entry == null;
		} catch (LDAPException e) {
			logger.error(e);
		}
		return result;
	}

	public int getUpdateDelayInSecs() {
		return config.getUpdateDelayInSecs();
	}

}
