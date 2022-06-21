package org.alefzero.padl.sources;

/**
 * Ldap source factory builder.
 */
public class LdapSourceFactory implements PadlSourceFactory {

    @Override
    public LdapSource getSourceInstance() {
        return new LdapSource();
    }

}
