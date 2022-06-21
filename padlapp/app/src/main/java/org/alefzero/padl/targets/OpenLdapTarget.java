package org.alefzero.padl.targets;

import java.lang.invoke.MethodHandles;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.PadlConfig;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a target to manipulate an openldap used as internal engine for padl.
 */
public class OpenLdapTarget implements PadlTarget {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "openldap";
    private LdapNetworkConnection conn;
    private boolean availability = false;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void prepareResources(PadlConfig config) throws PadlException {
        logger.debug("Preparing target resources.");

        String adminUser = String.format("cn=admin,%s", config.getRootDN());

        logger.debug("Opening connection with following parameters: {} with administrative cn [{}]", config.toString(),
                adminUser);

        LdapConnectionConfig ldapConfig = new LdapConnectionConfig();
        ldapConfig.setLdapHost(config.getHost());
        ldapConfig.setLdapPort(config.getPort());
        ldapConfig.setName(adminUser);
        ldapConfig.setCredentials(config.getAdminPassword());
        ldapConfig.setUseTls(false);
        conn = new LdapNetworkConnection(ldapConfig);
        try {
            conn.bind();
        } catch (LdapException e) {
            logger.error("Error opening connection with target ldap. Reason: ", e);
            throw new PadlException("Could not open resources with configuration data.");
        }
        availability = true;
    }

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

    @Override
    public boolean isReady() {
        return availability;
    }

}
