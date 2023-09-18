package org.alefzero.padl.config;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.alefzero.padl.sources.PadlSourceConfiguration;
import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PadlServiceManager {

	protected static final Logger logger = LogManager.getLogger();

	private Path configurationFile = null;
	private Map<String, PadlSourceFactory> sourceFactories = new HashMap<String, PadlSourceFactory>();
	private Map<String, PadlSourceService> sourceServices = new HashMap<String, PadlSourceService>();

	public PadlServiceManager(Path configurationFile) throws IOException {
		this.configurationFile = configurationFile;
		if (configurationFile != null && !Files.exists(configurationFile)) {
			throw new FileSystemException(
					String.format("Cannot process configuration file with value of %s", configurationFile));
		} else {
			loadAllSourceFactoriesTypes();
		}
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

	public PadlConfig getConfig() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode rootTree = mapper.readTree(configurationFile.toFile());
		PadlConfig config = mapper.treeToValue(rootTree.get("general"), PadlConfig.class);
		JsonNode sourcesParameters = rootTree.get("sourceParameters");
		if (sourcesParameters != null) {
			for (JsonNode setup : rootTree.get("sourceParameters")) {
				PadlSourceFactory targetFactory = sourceFactories.get((setup.get("type").asText()));
				targetFactory
						.setSourceParameters(mapper.treeToValue(setup, targetFactory.getSourceParameterClassType()));
			}
		}

		List<PadlSourceConfiguration> configSources = new LinkedList<PadlSourceConfiguration>();
		for (JsonNode sourceNode : rootTree.get("sources")) {
			logger.info("Reading configuration for source {} of type {}.", sourceNode.get("id").asText(),
					sourceNode.get("type").asText());
			PadlSourceFactory serviceFactory = sourceFactories.get((sourceNode.get("type").asText()));
			PadlSourceConfiguration sourceConfig = mapper.treeToValue(sourceNode, serviceFactory.getSourceConfigType());
			if (sourceConfig.isEnabled()) {
				configSources.add(sourceConfig);
				PadlSourceService service = serviceFactory.getService();
				service.setSourceParameters(serviceFactory.getSourceParameters());
				service.setConfig(sourceConfig);
				sourceConfig.setSource(service);
				sourceConfig.setFactory(serviceFactory);
				sourceServices.put(sourceConfig.getId(), service);
			}
		}
		logger.trace("Sources loaded: {}", configSources);
		config.setSources(configSources);
		return config;
	}

	public PadlSourceService getSourceById(String id) {
		return sourceServices.get(id);
	}

	public PadlSourceFactory getFactoryByType(String type) {
		return sourceFactories.get(type);
	}

}
