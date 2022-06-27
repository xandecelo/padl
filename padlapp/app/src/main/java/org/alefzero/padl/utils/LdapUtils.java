package org.alefzero.padl.utils;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapUtils {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public static Entry createEntry(String dn, String ldapType, String value, Collection<String> objectClasses,
            Collection<Attribute> attributes) throws LdapException {
        switch (ldapType) {
            case "localityName":
                break;
            case "stateOrProvinceName":
                break;
            case "countryName":
                break;
            case "streetAddress":
                break;

            case "organizationName":
                objectClasses.add("organization");
                attributes.add(new DefaultAttribute("o", value));
            case "domainComponent":
                objectClasses.add("dcObject");
                attributes.add(new DefaultAttribute("dc", value));
                break;

            case "organizationalUnitName":
                objectClasses.add("organizationalUnit");
                attributes.add(new DefaultAttribute("ou", value));
                attributes.add(new DefaultAttribute("objectClass", "organizationalUnit"));
                break;

            case "commonName":
            case "userId":
                dn = String.format(dn, value);
                break;

            default:
                logger.error("Type {} cannot be identified. Check your configuration syntax.",
                        ldapType);
        }
        Entry entry = new DefaultEntry(dn);
        addObjectClasses(entry, objectClasses);
        entry.add(attributes.toArray(new Attribute[0]));
        return entry;
    }

    private static void addObjectClasses(Entry entry, Collection<String> objectClasses, String... additionalClasses)
            throws LdapException {
        for (var objectClass : objectClasses) {
            entry.add("objectClass", objectClass);
        }
        for (var objectClass : additionalClasses) {
            entry.add("objectClass", objectClass);
        }
    }

}
