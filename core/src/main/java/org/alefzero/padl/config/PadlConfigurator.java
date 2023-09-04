package org.alefzero.padl.config;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.alefzero.padl.config.model.PadlGeneralConfig;
import org.alefzero.padl.config.model.PadlSourceConfig;
import org.alefzero.padl.config.model.PadlSourceFactory;
import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PadlConfigurator {

	protected static final Logger logger = LogManager.getLogger();

	private Path configurationFile = null;
	private PadlGeneralConfig generalConfig;
	private Map<String, PadlSourceFactory> sourceFactories = new HashMap<String, PadlSourceFactory>();

	public PadlConfigurator(Path configurationFile) throws FileSystemException {
		this.configurationFile = configurationFile;
		if (configurationFile != null && !Files.exists(configurationFile)) {
			throw new FileSystemException(
					String.format("Cannot process configuration file with value of %s", configurationFile));
		}
		loadAllSourceFactoriesTypes();
	}

	public String getTargetAdminConfig() {
		return new StringBuffer().append("# Change cn=admin,cn=config password")
				.append("\ndn: olcDatabase={0}config,cn=config").append("\nchangetype: modify")
				.append("\nreplace: olcRootPW").append("\nolcRootPW: %%LDAP_ROOT_PASSWORD%%").append("\n").append("\n")
				.append(getLdapDatabaseConfig()).toString();
	}

	public String getLdapDatabaseConfig() {
		try {
			loadYamlData();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("There's a problem with the configuration file.");
		}

		// get all configs
		// check if root DN is present
		// true - insert delete default mdb configuration
		// set passwd to rootdn

		return generalConfig.toString();
	}

	private void loadYamlData() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode rootTree = mapper.readTree(configurationFile.toFile());
		generalConfig = mapper.treeToValue(rootTree.get("general"), PadlGeneralConfig.class);
		generalConfig.setSources(getAllSources(mapper, rootTree.get("sources")));

	}

	private List<PadlSourceConfig> getAllSources(ObjectMapper mapper, JsonNode sourcesNode)
			throws JsonProcessingException, IllegalArgumentException {
		List<PadlSourceConfig> sources = new LinkedList<PadlSourceConfig>();
		for (JsonNode sourceNode : sourcesNode) {
			logger.info("Reading configuration for source {} of type {}.", sourceNode.get("id").asText(),
					sourceNode.get("type").asText());
			PadlSourceFactory targetFactory = getSourceFactoryByType(sourceNode.get("type").asText());
			PadlSourceConfig sourceConfig = mapper.treeToValue(sourceNode, targetFactory.getConfigClass());
			sources.add(sourceConfig);
		}
		logger.trace("Sources loaded: {}", sources);
		return sources;
	}

	private PadlSourceFactory getSourceFactoryByType(String type) {
		return sourceFactories.get(type);
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

	public String getLdifConfigurationForLdap() {
		StringBuffer _return = new StringBuffer();

		// remove default mdb if any source matches suffix of rootDN
		// for each source, get config
		// configure rootDN
		// if any target has a memberOf, apply 'dyngroup'

		return _return.toString();

	}

	public PadlGeneralConfig getGeneralConfig() {
		return Objects.requireNonNull(generalConfig, "Configuration has not been correctly initialized.");
	}

	public static void main(String[] args) {
		List<PadlSourceConfig> list = new ArrayList<PadlSourceConfig>();
		list.add(new PadlSourceConfig().setSuffix("dc=example,dc=org"));
		list.add(new PadlSourceConfig().setSuffix("dc=users,dc=example,dc=org"));
		list.add(new PadlSourceConfig().setSuffix("dc=groups,dc=example,dc=org"));
		list.add(new PadlSourceConfig().setSuffix("ou=inc1,dc=groups,dc=example,dc=org"));
		list.add(new PadlSourceConfig().setSuffix("ou=inc2,dc=groups,dc=example,dc=org"));
		Collections.sort(list);

		PadlGeneralConfig config = new PadlGeneralConfig().setSources(list);

		config.getSources().forEach(item -> System.out.println(item.getSuffix()));
		System.out.println();
		list.forEach(item -> System.out.println(item.getSuffix()));
		System.out.println();
		list.forEach(item -> System.out.println(item.getReversedSuffix()));
	}

}
