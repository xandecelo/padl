package org.alefzero.padl.sources;

import org.alefzero.padl.core.services.PadlSourceFactory;

/**
 * Ldap source factory builder.
 */
public class LdapSourceFactory extends PadlSourceFactory {

    @Override
    public LdapSource getInstance() {
        return new LdapSource();
    }

}
