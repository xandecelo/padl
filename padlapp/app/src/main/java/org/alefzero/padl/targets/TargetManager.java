package org.alefzero.padl.targets;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the creation of instances by their id
 * 
 */
public class TargetManager {

    private static Logger logger = LoggerFactory.getLogger(TargetManager.class);

    private static Map<String, PadlTarget> availableTargets = null;
    private static boolean configured = false;

    private static void loadTargets() {
        if (!configured) {
            availableTargets = new HashMap<String, PadlTarget>();
            ServiceLoader<PadlTargetFactory> servicesFactories = ServiceLoader.load(PadlTargetFactory.class);
            for (var targetServiceFactory : servicesFactories) {
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
    }

    public static PadlTarget getInstance(String type) {
        loadTargets();
        return availableTargets.get(type);
    }

}
