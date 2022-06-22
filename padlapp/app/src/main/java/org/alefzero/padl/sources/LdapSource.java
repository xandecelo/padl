package org.alefzero.padl.sources;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.core.model.LDAPSourceConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.utils.LdapUtils;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
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
            if (target.isReady()) {

            }
            SchemaLoader loaderDest = new DefaultSchemaLoader(target.getConnection());

            Entry entry = new DefaultEntry(target.getSchemaCN(config.getId()));
            entry.add("objectClass", "olcSchemaConfig");
            entry.add("cn", config.getId());

            for (Schema sourceSchema : loaderSource.getAllEnabled()) {
                logger.info("Processing source schema [{}].", sourceSchema.getSchemaName());
                Schema targetSchema = loaderDest.getSchema(sourceSchema.getSchemaName());
                for (SchemaObjectWrapper sourceSOW : sourceSchema.getContent()) {
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
                                elem = "'" + sourceSOW.get().getObjectType().name() + "'";
                        }
                        logger.trace("Configuration to be added to the target: [{}, {}]", elem, sourceSOW.get().getSpecification());
                        entry.add(elem, sourceSOW.get().getSpecification());
                    }
                }
            }
            configEntries.add(entry);
        } catch (LdapException e) {
            e.printStackTrace();
        }
        return configEntries;
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfig, PadlTarget target) {
        if (!(sourceConfig instanceof LDAPSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfig);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!target.isReady()) {
            logger.error("Target [{}] type is not ready.", target);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (LDAPSourceConfig) sourceConfig;
        this.target = target;
    }

    @Override
    public PadlTarget getTarget() {
        return this.target;
    }

}
