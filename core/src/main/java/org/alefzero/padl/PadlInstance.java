package org.alefzero.padl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alefzero.padl.config.PadlConfig;
import org.alefzero.padl.config.PadlServiceManager;
import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.alefzero.padl.sources.PadlSourceConfiguration;
import org.alefzero.padl.sources.PadlSourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldif.LDIFException;

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

	private void createDefaultOrganization() {
		try (LDAPConnection conn = getLDAPConnection()) {
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
		try (LDAPConnection conn = getLDAPConnection()) {
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

	public void checkBaseOrg() {
		if (this.instanceDontHaveOrganization()) {
			this.createDefaultOrganization();
		}
		checkForGapBetweenSuffixes();
	}

	private void checkForGapBetweenSuffixes() {
		List<PadlSourceConfiguration> sources = config.getSourcesInConfigurationOrder();
		boolean isMainSuffixEqualsAnySource = sources.stream()
				.anyMatch(source -> config.getSuffix().equals(source.getSuffix()));
		if (!isMainSuffixEqualsAnySource) {
			if (sources.size() == 1) {
				List<Entry> entries = new LinkedList<Entry>();
				String sourceSuffix = config.getSources().get(0).getSuffix();
				logger.warn("""
						A gap between main suffix {} and source suffix {} was detected.
						The system will try to fill the gaps automatically.
						In case of problematic behaviour, you can prevent this adding the entries manually.
						""", config.getSuffix(), sourceSuffix);
				String suffix = sourceSuffix;
				try {
					do {
						Entry entry = getEntryForSuffix(suffix);
						entries.add(0, entry);

						suffix = entry.getParentDNString();
						if (suffix == null) {
							logger.error("Source suffix {} is invalid under main suffix {}.", sourceSuffix,
									config.getSuffix());
							break;
						}
					} while (!suffix.equals(config.getSuffix()));

					try (LDAPConnection conn = getLDAPConnection()) {
						for (Entry entry : entries) {
							conn.add(entry);
						}
					}
				} catch (LDAPException | LDIFException e) {
					logger.error("""
							System couldn't resolve the gaps between suffixes automatically. Check your configuration.
							Caused by: {}
								""", e);
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage());
				}

			} else {
				logger.warn(
						"""
								A gap between main suffix {} and sources suffixes was detected and cannot be configured when source count > 1.
								This can make your data to not show in the LDAP tree.
								""",
						config.getSuffix());
			}

		}
	}

	private Entry getEntryForSuffix(String sourceSuffix) throws IllegalArgumentException, LDIFException {
		Entry entry = null;
		String[] objectSplit = sourceSuffix.split("=");
		if (objectSplit.length == 0) {
			throw new PadlUnrecoverableError(String.format("The suffix {} is invalid.", sourceSuffix));
		}
		String objectType = objectSplit[0];
		String objectValue = objectSplit[1].split(",")[0];
		String objectClass = "";
		switch (objectType) {
			case "ou":
				objectClass = "organizationalUnit";
				break;
			case "dc":
				objectClass = "domain";
				break;
			default:
				break;
		}

		if (objectClass.isEmpty()) {
			logger.warn("Automatic suffix only works with OU or DN elements");
			throw new IllegalArgumentException("Automatic suffix only works with OU or DN elements");
		}

		String ldif = String.format("""
				dn: %s
				objectClass: top
				objectClass: %s
				%s: %s
				""", sourceSuffix, objectClass, objectType, objectValue);
		logger.debug("Creating automatic entry with LDIF: \n{}", ldif);
		entry = new Entry(ldif.split("\n"));
		return entry;
	}

	private LDAPConnection getLDAPConnection() throws LDAPException {
		return new LDAPConnection("localhost", 389, "cn=admin," + config.getSuffix(),
				config.getAdminPassword());
	}

	public static void main(String[] args) throws IllegalArgumentException, LDIFException, LDAPException {
		String suffix = "ou=APP,ou=groups,dc=example,dc=org,dc=br";
		PadlInstance inst = new PadlInstance();
		Object parentSuffix = "dc=example,dc=org,dc=br";
		do {
			Entry entry = inst.getEntryForSuffix(suffix);
			System.out.println(entry);
			suffix = entry.getParentDNString();
			if (suffix == null) {
				System.out.println("Null found");
				break;
			}
		} while (!suffix.equals(parentSuffix));

	}
}
