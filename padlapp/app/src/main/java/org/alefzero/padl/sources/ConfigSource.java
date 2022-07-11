package org.alefzero.padl.sources;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.ConfigSourceConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSource extends PadlSource {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "config";

    private ConfigSourceConfig config;

    private PadlTarget target;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        List<Entry> entries = new LinkedList<Entry>();
        try {
            Dn dn = new Dn(String.format(getConfig().getDn(), getConfig().getId()));
            Entry entry = new DefaultEntry(dn);
            entry.add("objectClass", "olcSchemaConfig");
            entry.add("cn", config.getId());
            entry.add(config.getAttributeType(), config.getLdif());
            entries.add(entry);
        } catch (LdapException e) {
            logger.error("Cannot run configuration.", e);
        }
        return entries;
    }

    @Override
    public PadlSourceConfig getConfig() throws NullPointerException {
        if (config == null) {
            throw new NullPointerException("Service is not yet configured with setup method.");
        }
        return config;
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        if (!(sourceConfiguration instanceof ConfigSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!targetService.isReady()) {
            logger.error("Target [{}] type is not ready.", targetService);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (ConfigSourceConfig) sourceConfiguration;
        this.target = targetService;
        this.target.prepareForSourceLoading();
    }

    @Override
    public PadlTarget getTarget() {
        return this.target;
    }

    @Override
    protected void loadToTarget() throws PadlException {
        // DO NOTHING
        // This source was meant to run entries only at configuration tree.
    }

}
