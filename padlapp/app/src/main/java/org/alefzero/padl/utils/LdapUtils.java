package org.alefzero.padl.utils;

import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

public class LdapUtils {

    public static LdapNetworkConnection getConnection(String host, Integer port, boolean useTLS, String bindUser,
            String bindPassword) {
        /*
         * Use an instance object to connect in case of network blocks so multiple
         * requests can be handled
         * A synchronized block would stop multiple threads, what is non optimal.
         * A side efect is no control of how much connections could be requested as
         * memory hog (and can be improved)
         */
        return new LdapUtils().connect(host, port, useTLS, bindUser, bindPassword);
    }

    private LdapNetworkConnection connect(String host, Integer port, boolean useTLS, String bindUser,
            String bindPassword) {
        LdapConnectionConfig ldapConfig = new LdapConnectionConfig();
        ldapConfig.setLdapHost(host);
        ldapConfig.setLdapPort(port);
        ldapConfig.setUseTls(useTLS);
        ldapConfig.setName(bindUser);
        ldapConfig.setCredentials(bindPassword);
        return new LdapNetworkConnection(ldapConfig);
    }

}
