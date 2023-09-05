/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.alefzero.padl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

import org.alefzero.padl.config.PadlInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
	protected static final Logger logger = LogManager.getLogger();

	private static final String DEFAULT_ACTION = "help";
	private static final String DEFAULT_CONFIGURATION_FILENAME = System.getenv("$APP_DIR") + "./conf/padl.yaml";
	private ScheduledFuture<?> executor = null;

	public static void main(String[] args) {
		logger.info("Padl is starting");
		logger.debug("Padl is starting with parameters %s", Arrays.toString(args));
		String configurationFilename = getConfigurationFilename(args);
		String action = args.length > 1 ? args[1] : DEFAULT_ACTION;
		String sourceType = args.length > 2 ? args[2] : "";
		new App().startAction(action, configurationFilename, sourceType);
	}

	private void startAction(String action, String configurationFilename, String sourceType) {
		logger.trace(".startAction [action: {}, configurationFilename: {}]", action, configurationFilename);
		try {
			Path configurationFile = Paths.get(configurationFilename);
			PadlInstance instance = new PadlInstance(configurationFile);
			switch (action.toLowerCase()) {
			case "check-yaml":
				checkYAML(instance);
				break;
			case "get-os-variables":
				getGlobalOSVariables(instance);
				break;
			case "check-connectivity":
				checkConnectivity(instance);
				break;
			case "ldap-setup":
				getAdminConfiguration(instance);
				break;
			case "source-os-config-list":
				getSourceOSScriptList(instance);
				break;
			case "source-env-config":
				getSourceOSEnv(instance, sourceType);
				break;
			case "sync":
				runSyncProcess(instance);
				break;
			case "help":
			default:
				this.help();
				break;
			}
			System.exit(0);
		} catch (IOException e) {
			logger.error("Error processing action {}: ", action.toLowerCase(), e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void getGlobalOSVariables(PadlInstance instance) {
		System.out.print(instance.getGlobalOSVariables());
		
	}

	private void checkConnectivity(PadlInstance instance) throws IOException {
		instance.checkConnectivity();

	}

	private void checkYAML(PadlInstance instance) throws IOException {
		instance.checkYAML();
	}

	private void getSourceOSEnv(PadlInstance instance, String sourceType) throws IOException {
		System.out.println(instance.getSourceOSEnvFor(sourceType));
	}

	public static String getConfigurationFilename(String[] args) {
		logger.trace(".getConfigurationFilename [args: {}]", Arrays.toString(args));
		String configurationFilename = args.length > 0 ? args[0] : "";
		configurationFilename = configurationFilename.isEmpty() ? DEFAULT_CONFIGURATION_FILENAME
				: configurationFilename;
		logger.trace(".getConfigurationFilename [return: {}]", configurationFilename);
		return configurationFilename;
	}

	private void getAdminConfiguration(PadlInstance instance) throws IOException {
		System.out.println(instance.getLdapAdminConfig());
	}

	private void getSourceOSScriptList(PadlInstance instance) throws IOException {
		System.out.println(instance.getSourceOsConfig());
	}

	private void help() {
		logger.trace(".help");
		String help = """

				Padl - an easy proxy ldap configurator.
				Usage: run.sh configuration_file.yaml help|admin-config|run|source-os-config-list

				""";
		logger.info(help);
		System.out.println(help);
	}

	private void runSyncProcess(PadlInstance instance) {

		Thread shutdownListener = new Thread() {
			public void run() {
				logger.info("Requesting padl processes to stop (10s)...");
				try {
					if (executor != null) {
						executor.cancel(false);
						Thread.sleep(10000);
					}
					logger.info("Padl is shutdown.");
				} catch (InterruptedException e) {
					logger.error("Aborting...");
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownListener);
		// Thread.sleep(20);
		logger.info("Padl is done.");

	}
}
