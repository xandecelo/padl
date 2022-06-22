package org.alefzero.padl.core.services;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface of any data source used by padl to generate data with provided
 * configuration.
 */
public abstract class PadlSource {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PadlSourceConfig config;

    abstract public String getId();

    /**
     * Generate a list of configuration entries to be processed by the target in a
     * configuration phase
     * Custom object classes and attributes are examples of entries.
     * It should never return null - in case of no configuration this must return an
     * empty list.
     *
     * @return
     */
    public abstract List<Entry> getConfigurationEntries();

    /**
     * @return the configuration associated with this source.
     * @throws NullPointerException with not yet configured
     */

    public PadlSourceConfig getConfig() throws NullPointerException {
        if (config == null) {
            throw new NullPointerException("Service is not yet configured with setup method.");
        }
        return config;
    }

    /**
     * Entangle this source service to run with a configuration and a target
     * service and do all the preparation necessary to start the data processing
     * 
     * @param sourceConfiguration to be used
     * @param targetService       used as data destination
     */
    public abstract void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService);

    /**
     * Return the entangled target with this source
     * 
     * @return
     */
    public abstract PadlTarget getTarget();

    /**
     * Orchestrate the configuration and loading phases from source to the target.
     * 
     * @throws PadlException
     */
    public void orchestrateProcess() throws PadlException {
        configureTarget();
        loadToTarget();
    }

    /**
     * Do the configuration steps required before loading actual data.
     * 
     * @throws PadlException
     */
    protected void configureTarget() throws PadlException {
        if (getTarget() != null) {
            getTarget().addConfiguration(getConfigurationEntries());
        }
    }

    /**
     * Offer data to be loaded to the target.
     */
    public void loadToTarget() {

    }

}
