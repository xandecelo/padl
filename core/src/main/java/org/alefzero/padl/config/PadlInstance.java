package org.alefzero.padl.config;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.alefzero.padl.sources.PadlSourceServiceConfig;
import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceFactorySetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PadlInstance {

	protected static final Logger logger = LogManager.getLogger();

	private Path configurationFile = null;
	private PadlGeneralConfig generalConfig;
	private Map<String, PadlSourceFactory> sourceFactories = new HashMap<String, PadlSourceFactory>();

	public PadlInstance(Path configurationFile) throws IOException {
		this.configurationFile = configurationFile;
		if (configurationFile != null && !Files.exists(configurationFile)) {
			throw new FileSystemException(
					String.format("Cannot process configuration file with value of %s", configurationFile));
		} else {
			loadAllSourceFactoriesTypes();
			loadYAMLData();
		}
	}

	public String getLdapAdminConfig() {
		StringBuffer sb = new StringBuffer().append("# Change cn=admin,cn=config password");
		sb.append("\ndn: olcDatabase={0}config,cn=config");
		sb.append("\nchangetype: modify");
		sb.append("\nreplace: olcRootPW");
		sb.append("\nolcRootPW: %%LDAP_ROOT_PASSWORD%%");
		sb.append("\n").append("\n");
		sb.append(generalConfig.getLDIFConfiguration()).toString();
		return sb.toString();
	}

	private void loadYAMLData() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode rootTree = mapper.readTree(configurationFile.toFile());
		generalConfig = mapper.treeToValue(rootTree.get("general"), PadlGeneralConfig.class);

		for (JsonNode setup : rootTree.get("sourceFactoriesSetup")) {
			PadlSourceFactory targetFactory = sourceFactories.get((setup.get("type").asText()));
			targetFactory.setFactorySetup(mapper.treeToValue(setup, targetFactory.getSourceFactorySetupClass()));
		}

		List<PadlSourceServiceConfig> sources = new LinkedList<PadlSourceServiceConfig>();
		for (JsonNode sourceNode : rootTree.get("sources")) {
			logger.info("Reading configuration for source {} of type {}.", sourceNode.get("id").asText(),
					sourceNode.get("type").asText());
			PadlSourceFactory targetFactory = sourceFactories.get((sourceNode.get("type").asText()));
			PadlSourceServiceConfig sourceConfig = mapper.treeToValue(sourceNode, targetFactory.getServiceConfigType());
			if (sourceConfig.isEnabled()) {
				sources.add(sourceConfig);
			}
		}
		logger.trace("Sources loaded: {}", sources);

		generalConfig.setSources(sources);
	}

	private void loadAllSourceFactoriesTypes() {
		logger.trace(".loadAllSourceFactoriesTypes.");
		ServiceLoader<PadlSourceFactory> serviceFactories = ServiceLoader.load(PadlSourceFactory.class);
		serviceFactories.forEach(serviceFactory -> {
			logger.debug("Loading source component {} (id: {}).", serviceFactory.getClass().getSimpleName(),
					serviceFactory.getServiceType());

			var serviceFactoryWithSameType = sourceFactories.putIfAbsent(serviceFactory.getServiceType(),
					serviceFactory);

			if (serviceFactoryWithSameType != null) {
				String message = String.format(
						"Service type {} is already registered. Aborting since this can produce unexpected behavior. (Name: {}, Class: {})",
						serviceFactory.getServiceType(), serviceFactory.getClass().getSimpleName(),
						serviceFactoryWithSameType.getClass().getSimpleName());
				logger.error(message);
				throw new PadlUnrecoverableError(message);
			}

		});
		logger.trace(".loadAllSourceFactoriesTypes. [return]");
	}

	@Deprecated
	public String getSourceOsConfig() {
		return generalConfig.getSourceOSConfig();
	}

	public String getSourceOSEnvFor(String sourceType) {

		String result = "";

		return result;
	}

	public void checkConnectivity() {
		// TODO Auto-generated method stub
		// loop em todos os sources
		// testar conectividade

	}

	public void checkYAML() {
		// TODO Auto-generated method stub
		// loop em todos os source
		// verificar inconsistencias
	}

	public String getGlobalOSVariables() {
		return "LDAP_ADM_PASSWORD=" + generalConfig.getAdminPassword();
	}

}
