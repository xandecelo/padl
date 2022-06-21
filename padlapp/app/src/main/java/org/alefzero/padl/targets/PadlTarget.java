package org.alefzero.padl.targets;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;

/**
 * Default interface of a padl target
 */
public interface PadlTarget extends AutoCloseable {

    String getId();

    /**
     * Prepare resources to be used with this target. Configurations, connections
     * and any requirement can be used here.
     * This method could be called everytime over a configuration to refresh
     * resources, even with same configuration object.
     * 
     * @param config target configuration from PadlConfig
     * @throws PadlException
     */
    void prepareResources(PadlConfig config) throws PadlException;

    /**
     * Close all resources used by this target.
     * In case of failure, no Exception has to been thrown, since assumes a close
     * action is the last one to be issued.
     * If you need to indicate a problem with a closed resource, use changes to
     * {@link #isReady()} method to signal this state.
     */
    void close();

    /**
     * Indicate if this target is okay to proceed with operations
     */
    boolean isReady();

}
