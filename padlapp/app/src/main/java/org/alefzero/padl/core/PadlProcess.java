package org.alefzero.padl.core;

import java.lang.invoke.MethodHandles;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processing features of this padl instance.
 */
public class PadlProcess {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PadlConfig config = new PadlConfig();

    public PadlProcess(PadlConfig config) {
        this.config = config;
    }

    public void run() {
        PadlTarget target = PadlServicesManager.getTargetInstance(config.getType());
        try (target) {
            target.prepareResources(config);
            for (PadlSourceConfig sourceConfig : config.getSources()) {
                PadlSource source =  PadlServicesManager.getSourceInstance(sourceConfig.getType());
                logger.info("Processing config {} for source {}.", sourceConfig, source);
                source.entangle(sourceConfig, target);
                source.orchestrateProcess();
            }
        } catch (PadlException e) {
            logger.error("Error processing data: {}", e.getMessage());
        }
    }

}
