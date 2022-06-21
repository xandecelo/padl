package org.alefzero.padl.core;

import java.lang.invoke.MethodHandles;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;
import org.alefzero.padl.targets.PadlTarget;
import org.alefzero.padl.targets.TargetManager;
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
        PadlTarget target = TargetManager.getInstance(config.getType());
        try (target) {
            target.prepareResources(config);
            if (target.isReady()) {
            }
        } catch (PadlException e) {
            logger.error("Error processing data: {}", e.getMessage());
        }
    }

}
