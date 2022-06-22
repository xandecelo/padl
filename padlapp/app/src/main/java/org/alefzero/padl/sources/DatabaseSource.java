package org.alefzero.padl.sources;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.core.model.DatabaseSourceConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Database source is used to import data from any SLQ data structure and
 * convert to a ldap format.
 */
public class DatabaseSource extends PadlSource {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "database";
    private PadlTarget target;
    private DatabaseSourceConfig config;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        return new LinkedList<Entry>();
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        if (!(sourceConfiguration instanceof DatabaseSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!target.isReady()) {
            logger.error("Target [{}] type is not ready.", target);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (DatabaseSourceConfig) sourceConfiguration;
        this.target = targetService;
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
}
