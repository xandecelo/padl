package org.alefzero.padl.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlSourceFactory;
import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.core.services.PadlTargetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the creation of instace services (sources and targets) by their id
 */
public class PadlServicesManager {

    private static Logger logger = LoggerFactory.getLogger(PadlServicesManager.class);

    private static Map<String, PadlTarget> availableTargets = null;
    private static Map<String, PadlSource> availableSources = null;

    private static boolean targetsConfigured = false;
    private static boolean sourcesConfigured = false;

    private static void loadTargets() {
        logger.debug ("Loading target services...");
        if (!targetsConfigured) {
            availableTargets = new HashMap<String, PadlTarget>();
            ServiceLoader<PadlTargetFactory> servicesFactories = ServiceLoader.load(PadlTargetFactory.class);
            for (PadlTargetFactory targetServiceFactory : servicesFactories) {
                PadlTarget targetService = targetServiceFactory.getInstance();
                logger.debug("Loading target {} with id [{}]", targetService.getClass().getSimpleName(),
                        targetService.getId());
                PadlTarget loadedItem = availableTargets.putIfAbsent(targetService.getId(), targetService);
                if (null != loadedItem) {
                    logger.error("Target {} cannot be loaded because id [{}] is already in use by {}.",
                            targetService.getClass().getSimpleName(), targetService.getId(),
                            loadedItem.getClass().getSimpleName());
                }
            }
        }
        targetsConfigured = true;
    }

    private static void loadSources() {
        logger.debug ("Loading source services...");
        if (!sourcesConfigured) {
            availableSources = new HashMap<String, PadlSource>();
            ServiceLoader<PadlSourceFactory> servicesFactories = ServiceLoader.load(PadlSourceFactory.class);
            for (PadlSourceFactory sourceServiceFactory : servicesFactories) {
                PadlSource sourceService = sourceServiceFactory.getInstance();
                logger.debug("Loading source service {} with id [{}]", sourceService.getClass().getSimpleName(),
                        sourceService.getId());
                PadlSource loadedItem = availableSources.putIfAbsent(sourceService.getId(), sourceService);
                if (null != loadedItem) {
                    logger.error("Source service {} cannot be loaded because id [{}] is already in use by {}.",
                            sourceService.getClass().getSimpleName(), sourceService.getId(),
                            loadedItem.getClass().getSimpleName());
                }
            }
        }
        sourcesConfigured = true;
    }

    public static PadlTarget getTargetInstance(String type) {
        loadTargets();
        return availableTargets.get(type);
    }

    public static PadlSource getSourceInstance(String type) {
        loadSources();
        return availableSources.get(type);
    }

    protected static Collection<PadlSource> getAllSources() {
        loadSources();
        return availableSources.values();
    }

    protected static Collection<PadlTarget> getAllTargets() {
        loadSources();
        return availableTargets.values();
    }
}
