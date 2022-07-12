package org.alefzero.padl.core.services;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alefzero.padl.core.exceptions.PadlConfigurationException;
import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.utils.LdapUtils;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyRequest;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchAttributeException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaViolationException;
import org.apache.directory.api.util.Strings;
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
    private static int oidCount = 0;
    private Set<String> processedDNs = new HashSet<String>();

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
            this.conn = LdapUtils.getConnection(config.getHost(), config.getPort(), false, config.getBindCn(),
                    config.getAdminPassword());
            this.conn.bind();
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
        addEntry(entry, PadlSourceConfig.LOAD_STRATEGY_MERGE);
    }

    /**
     * Process a list of entries to add
     * 
     * @throws PadlException when process fails
     */
    public void addEntry(Entry sourceEntry, String loadStrategy) throws PadlException {
        logger.trace("Adding entry {} with strategy {}.", sourceEntry, loadStrategy);
        String processingAttributeName = null;
        try {
            // try to add, dont matter strategy
            getConnection().add(sourceEntry);
        } catch (LdapSchemaViolationException | LdapEntryAlreadyExistsException entryExistsException) {
            // entry exists, so try one of strategies
            try {

                switch (loadStrategy.toLowerCase()) {
                    case PadlSourceConfig.LOAD_STRATEGY_IGNORE:
                        logger.info("Entry {} already exists at the target LDAP. Ignoring...", sourceEntry.getDn());
                        logger.trace("Entry detail {}", sourceEntry);
                        break;
                    case PadlSourceConfig.LOAD_STRATEGY_REPLACE:
                        // Check if this entry has been already replaced by the source.
                        // if so, new data from this source MUST merge.
                        if (!processedDNs.contains(sourceEntry.getDn().toString())) {
                            getConnection().delete(sourceEntry.getDn());
                            getConnection().add(sourceEntry);
                            processedDNs.add(sourceEntry.getDn().toString());
                            break;
                        }
                    case PadlSourceConfig.LOAD_STRATEGY_ADD_ATTRIBUTE:
                        processingAttributeName = addAttributesFrom(sourceEntry);
                        break;
                    // Merge strategy is the default.
                    case PadlSourceConfig.LOAD_STRATEGY_MERGE:
                    default:
                        processingAttributeName = mergeAttributesFrom(sourceEntry);
                }

            } catch (LdapNoSuchAttributeException e) {
                logger.error("Error processing attribute: {} for entry {}.", processingAttributeName,
                        sourceEntry, e);
                throw new PadlException(e);
            } catch (LdapException e) {
                logger.error("Error processing DN data ({}). Entry details: {}", sourceEntry.getDn(),
                        sourceEntry, e);
                throw new PadlException(e);
            }
        } catch (LdapException ldapException) {
            logger.error("Error processing DN data ({}). Entry details: {}", sourceEntry.getDn(),
                    sourceEntry, ldapException);
            throw new PadlException(ldapException);
        }
    }

    private String addAttributesFrom(Entry sourceEntry) throws LdapException {
        String processingAttributeName = null;
        if (getConnection().exists(sourceEntry.getDn())) {
            for (Attribute sourceAttribute : sourceEntry.getAttributes()) {
                if (!"objectClass".equalsIgnoreCase(sourceAttribute.getId())) {
                    // TODO implement algorhythm specific for userpassword.
                    processingAttributeName = sourceAttribute.getId();
                    Modification mod = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,
                            processingAttributeName,
                            "{SHA}" + sourceAttribute.get().toString());
                    getConnection().modify(sourceEntry.getDn(), mod);

                }
            }
        }

        // else {
        // logger.warn("DN {} not found at target schema to add attributes.",
        // sourceEntry.getDn());
        // }
        return processingAttributeName;
    }

    private String mergeAttributesFrom(Entry sourceEntry) throws LdapException {
        String processingAttributeName = null;
        List<Modification> mods = new LinkedList<Modification>();
        for (Attribute sourceAttribute : sourceEntry.getAttributes()) {
            processingAttributeName = sourceAttribute.getId();
            boolean addThisAttribute = false;
            try {
                addThisAttribute = getConnection().compare(sourceEntry.getDn(), sourceAttribute.getId(),
                        sourceAttribute.get());

            } catch (LdapNoSuchAttributeException processingException) {
                // DO NOTHING. Attribute was not found at target entry and addThisAttribute is
                // already false;
            }
            if (!addThisAttribute) {
                // Default 'modification mode' is to add. Since custom attributes are
                // single-value, change to replace

                Modification modification = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,
                        sourceAttribute.getId(), sourceAttribute.get());
                mods.add(modification);
            }
        }
        if (!mods.isEmpty()) {
            getConnection().modify(sourceEntry.getDn(), mods.toArray(new Modification[0]));
        }
        return processingAttributeName;
    }

    protected abstract String getRootCN();

    public void addConfiguration(List<Entry> configurationEntries) throws PadlConfigurationException {
        Entry processingEntry = null;
        try (LdapNetworkConnection rootConn = LdapUtils.getConnection(config.getHost(), config.getPort(),
                config.getUseTLS(), this.getRootCN(), config.getRootConfigPassword())) {
            rootConn.bind();
            for (Entry configurationEntry : configurationEntries) {
                processingEntry = configurationEntry;
                rootConn.add(configurationEntry);
            }
        } catch (LdapException e) {
            logger.error("Error at configuration phase of target ldap.", e);
            logger.trace("Error caused while adding generated entry configuration: {}", processingEntry);
            throw new PadlConfigurationException(e);
        }
    }

    /**
     * Return the current ldap connection to the target being used.s
     * 
     * @return
     */
    public LdapNetworkConnection getConnection() {
        return this.conn;
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

    /*
     * Use this OID to generate custom attributes from the sources.
     * For example: if you inform the OID 1.3.6.1.4.1.99999.99 this target will
     * generate attributes
     * sequencially, adding an increment at the end of the identifier:
     * 
     * attribute1 - 1.3.6.1.4.1.99999.99.1
     * attribute2 - 1.3.6.1.4.1.99999.99.2 ... and so on
     * 
     * Remember to specify an OID with a code you own and use a number at the end
     * always available.
     * If a code is already in use, the attribute won't be created, and possibily
     * the data will be lost.
     * 
     */
    public String getNextOID() {
        return String.format(config.getOid() + ".%d", ++oidCount);
    }

    /**
     * Prepare all resources necessary right before a source loads. Used for
     * cleaning jobs.
     */
    public void prepareForSourceLoading() {
        this.processedDNs = new HashSet<String>();
    }
}
