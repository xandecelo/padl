package org.alefzero.padl.sources;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.LDAPSourceConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.utils.LdapUtils;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LdapSource is used to get data from an LDAP X.500 compliant implementation
 * according with provided configuration.
 */
public class LdapSource extends PadlSource {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private LDAPSourceConfig config;
    private PadlTarget target;

    private static final String ID = "ldap";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        List<Entry> configEntries = new LinkedList<Entry>();

        LdapNetworkConnection sourceConn = LdapUtils.getConnection(config.getHost(), config.getPort(),
                config.getUseTLS(),
                config.getBindCN(), config.getBindPassword());

        try (sourceConn) {
            sourceConn.bind();
            SchemaLoader loaderSource = new DefaultSchemaLoader(sourceConn);
            SchemaLoader loaderDest = new DefaultSchemaLoader(target.getConnection());

            Entry entry = new DefaultEntry(target.getSchemaCN(config.getId()));
            entry.add("objectClass", "olcSchemaConfig");
            entry.add("cn", config.getId());

            Set<String> targetAttributes = new HashSet<String>();

            for (Schema schema : loaderDest.getAllEnabled()) {
                for (Entry configEntry : loaderDest.loadAttributeTypes(schema)) {
                    targetAttributes.add(configEntry.get("m-name").get().getString());
                }
            }

            for (Schema sourceSchema : loaderSource.getAllEnabled()) {
                logger.info("Processing source configuration schema [{}].", sourceSchema.getSchemaName());
                Schema targetSchema = loaderDest.getSchema(sourceSchema.getSchemaName());

                for (SchemaObjectWrapper sourceSOW : sourceSchema.getContent()) {
                    String sourceAttributeName = sourceSOW.get().getName();
                    String sourceAttributeType = sourceSOW.get().getObjectType().name();
                    if (targetAttributes.contains(sourceAttributeName)) {
                        logger.trace("Attribute {} already configured at target. Skipping...", sourceAttributeName);
                        continue;
                    }

                    if (!targetSchema.getContent().contains(sourceSOW)) {
                        String elem;
                        switch (sourceSOW.get().getObjectType()) {
                            case OBJECT_CLASS:
                                elem = "olcObjectClasses";
                                break;
                            case ATTRIBUTE_TYPE:
                                elem = "olcAttributeTypes";
                                break;
                            default:
                                elem = "'" + sourceAttributeType + "'";
                        }
                        logger.trace("Configuration to be added to the target: [{}, {}]", elem,
                                sourceSOW.get().getSpecification());
                        entry.add(elem, sourceSOW.get().getSpecification());
                    }
                }
            }
            configEntries.add(entry);
        } catch (IOException | LdapException e) {
            logger.error("Cannot configure target attributes.", e);
        }
        return configEntries;
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        if (!(sourceConfiguration instanceof LDAPSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!targetService.isReady()) {
            logger.error("Target [{}] type is not ready.", targetService);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (LDAPSourceConfig) sourceConfiguration;
        this.target = targetService;
        this.target.prepareForSourceLoading();
    }

    @Override
    public PadlTarget getTarget() {
        return this.target;
    }

    @Override
    public PadlSourceConfig getConfig() throws NullPointerException {
        if (config == null) {
            throw new NullPointerException("Service is not yet configured with setup method.");
        }
        return config;
    }

    @Override
    protected void loadToTarget() throws PadlException {
        try (LdapNetworkConnection sourceConn = LdapUtils.getConnection(config.getHost(), config.getPort(),
                config.getUseTLS(),
                config.getBindCN(), config.getBindPassword())) {
            sourceConn.bind();
            Dn dn = new Dn(getConfig().getDn());
            // IMP: filter can be parametrized in YAML
            String filter = "(objectClass=*)";
            String[] searchAttributes = new String[0];
            EntryCursor entryCursor = sourceConn.search(dn, filter, SearchScope.SUBTREE, searchAttributes);
            // IMP: change dn target mapping
            for (Entry entry : entryCursor) {
                getTarget().addEntry(entry, config.getLoadStrategy());
            }
        } catch (LdapException e) {
            logger.error("Error processing ldap source:", e);
            throw new PadlException(e);
        }
    }
}
