package org.alefzero.padl.core.services;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.alefzero.padl.core.exceptions.PadlConfigurationException;
import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;
import org.alefzero.padl.utils.LdapUtils;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default abstract class of a padl target
 */
public abstract class PadlTarget implements GenericService {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LdapNetworkConnection conn = null;
    private boolean availability = false;
    private PadlConfig config = null;

    public abstract String getId();

    /**
     * Prepare resources to be used with this target. Configurations, connections
     * and any requirement can be used here.
     * This method could be called everytime over a configuration to refresh
     * resources, even with same configuration object.
     * 
     * @param config target configuration from PadlConfig
     * @throws PadlException
     */
    public void prepareResources(PadlConfig config) throws PadlException {
        logger.debug("Preparing target resources.");
        logger.debug("Opening connection with following parameters: {} with administrative cn [{}]", config.toString(),
                config.getBindCn());

        try {
            conn = LdapUtils.getConnection(config.getHost(), config.getPort(), false, config.getBindCn(),
                    config.getAdminPassword());
            conn.bind();
        } catch (LdapException e) {
            logger.error("Error opening connection with target ldap. Reason: ", e);
            throw new PadlException("Could not open resources with configuration data.");
        }

        this.config = config;
        availability = true;
    }

    /**
     * Process a list of entries to add
     * 
     * @throws PadlException when process fails
     */
    public void addEntry(Entry entry) throws PadlException {
        addEntry(entry, false);
    }

   /**
     * Process a list of entries to add
     * 
     * @throws PadlException when process fails
     */
    public void addEntry(Entry entry, boolean modify) throws PadlException {
        logger.debug("Adding entry {}", entry);
        try {
            if (! getConnection().exists(entry.getDn())) {
                getConnection().add(entry);
            } else {
                if (modify) {
                    getConnection().modify(entry, ModificationOperation.ADD_ATTRIBUTE);
                } else {
                    logger.info("Entry {} already exists at the target LDAP. Ignoring...", entry.getDn());
                    logger.debug("Entry detail ", entry);
                }
            }
        } catch (LdapException e) {
            throw new PadlException(e);
        }
    }


    protected abstract String getRootCN();

    public void addConfiguration(List<Entry> configurationEntries) throws PadlConfigurationException {
        try (LdapNetworkConnection rootConn = LdapUtils.getConnection(config.getHost(), config.getPort(),
                config.getUseTLS(), this.getRootCN(), config.getRootConfigPassword())) {
            rootConn.bind();
            for (Entry configurationEntry : configurationEntries) {
                rootConn.add(configurationEntry);
            }
        } catch (LdapException e) {
            logger.error("Error at configuration phase of target ldap. Cause: {}", e);
            throw new PadlConfigurationException(e);
        }
    }

    /**
     * Return the current ldap connection to the target being used.s
     * 
     * @return
     */
    public LdapNetworkConnection getConnection() {
        return conn;
    }

    /**
     * Return the name of the schema tree under current target
     * 
     * @param name
     * @return
     */
    public String getSchemaCN(String name) {
        return String.format("cn=%s,cn=schema,cn=config", name);
    }

    /**
     * Indicate if this target service is ready to use.
     * 
     * @return
     */
    public boolean isReady() {
        return availability;
    }

    /**
     * Close all resources used by this target.
     * In case of failure, no Exception has to been thrown, since assumes a close
     * action is the last one to be issued.
     * If you need to indicate a problem with a closed resource, use changes to
     * {@link #isReady()} method to signal this state.
     */
    @Override
    public void close() {
        logger.debug("Closing target resources.");
        availability = false;
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // DO NOTHING
            }
        }
    }
}
