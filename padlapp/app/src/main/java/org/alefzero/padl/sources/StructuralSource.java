package org.alefzero.padl.sources;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.model.StructuralSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.utils.LdapUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuralSource extends PadlSource {
    
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "structural";
    private StructuralSourceConfig config;
    private PadlTarget target;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        return new LinkedList<Entry>();
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
        if (!(sourceConfiguration instanceof StructuralSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!target.isReady()) {
            logger.error("Target [{}] type is not ready.", target);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (StructuralSourceConfig) sourceConfiguration;
        this.target = targetService;        
    }

    @Override
    public PadlTarget getTarget() {
        return this.target;
    }

    @Override
    protected void loadToTarget() throws PadlException {
        logger.debug("Processing structural element...");
        List<Attribute> attributes = new LinkedList<Attribute>();
        if (null != config.getAttributes()) {
            for (String attribute : config.getAttributes()) {
                StringTokenizer stEqual = new StringTokenizer(attribute, "=");
                String key = stEqual.nextToken().trim();
                String value = stEqual.countTokens() == 0 ? key : stEqual.nextToken().trim();
                attributes.add(new DefaultAttribute(key, value));
            }
        }
        try {
            target.addEntry(LdapUtils.createEntry(config.getDn(), config.getLdapType(), config.getValue(), config.getObjectClasses(), attributes));
        } catch (LdapException e) {
            throw new PadlException(e);
        }
    }
    
}
