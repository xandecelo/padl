package org.alefzero.padl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

	protected static final Logger logger = LogManager.getLogger();

	private static final String DEFAULT_ACTION = "help";
	private static final String DEFAULT_CONFIGURATION_FILENAME = System.getenv("$APP_DIR") + "./conf/padl.yaml";

	private ScheduledFuture<?> executor = null;

	public static void main(String[] args) {
		logger.info("Padl is starting");
		logger.debug("Padl is starting with parameters {}", Arrays.toString(args));
		String configurationFilename = getConfigurationFilename(args);
		String action = args.length > 1 ? args[1] : DEFAULT_ACTION;
		String sourceType = args.length > 2 ? args[2] : "";
		new App().startAction(action, configurationFilename, sourceType);
	}

	private void startAction(String action, String configurationFilename, String sourceType) {
		logger.trace(".startAction [action: {}, configurationFilename: {}, sourceType: {}]", action,
				configurationFilename, sourceType);
		try {
			Path configurationFile = Paths.get(configurationFilename);
			PadlInstance instance = new PadlInstance();
			instance.loadConfiguration(configurationFile);
			switch (action.toLowerCase()) {
				case "check-yaml":
					instance.checkYAML();
					break;
				case "get-os-variables":
					instance.getGlobalOSVariables();
					break;
				case "check-connectivity":
					instance.checkConnectivity();
					break;
				case "ldap-setup":
					instance.getLDIFSetupConfiguration();
					break;
				case "source-os-script-list":
					instance.getSourceOSScriptList();
					break;
				case "source-get-os-variables":
					instance.getSourceParameterOSEnv(sourceType);
					break;
				case "sync":
					runSyncProcess(instance);
					break;
				case "prepare":
					runSyncOnce(instance);
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

	public static String getConfigurationFilename(String[] args) {
		logger.trace(".getConfigurationFilename [args: {}]", Arrays.toString(args));
		String configurationFilename = args.length > 0 ? args[0] : "";
		configurationFilename = configurationFilename.isEmpty() ? DEFAULT_CONFIGURATION_FILENAME
				: configurationFilename;
		logger.trace(".getConfigurationFilename [return: {}]", configurationFilename);
		return configurationFilename;
	}

	private void help() {
		logger.trace(".help");
		String help = """

				Padl - an easy proxy ldap configurator.
				Usage: run.sh configuration_file.yaml help|admin-config|run|source-os-config-list|source-get-os-variables

				""";
		logger.info(help);
		System.out.println(help);
	}

	private void runSyncOnce(PadlInstance instance) {
		instance.prepareSync();
		instance.sync();
	}

	private void runSyncProcess(PadlInstance instance) {

		instance.prepareSync();

		Thread shutdownListener = new Thread() {
			public void run() {
				logger.info("Requesting padl processes to stop...");
				try {
					if (executor != null) {
						executor.cancel(false);
						Thread.sleep(1_000);
					}
					logger.info("Padl is shutdown.");
				} catch (InterruptedException e) {
					logger.error("Aborting...");
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownListener);

		try {
			executor = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
				try {
					logger.trace("Requesting sync process.");
					instance.sync();
				} catch (Exception | Error e) {
					logger.error("Sync error: {}", e.getCause(), e);
				}
			}, 0, instance.getUpdateDelayInSecs(), TimeUnit.SECONDS);

			while (true) {
				System.out.print("Press q and [Enter] to quit.");
				String data = System.console().readLine();
				if ("q".equalsIgnoreCase(data)) {
					break;
				}

			}
		} catch (PadlUnrecoverableError e) {
			e.printStackTrace();
			logger.error(e);
		}

		logger.info("Padl is done.");

	}
}
