package org.alefzero.padl.sources;

import org.alefzero.padl.core.services.PadlSourceFactory;

/**
 * Ldap source factory builder.
 */
public class DatabaseSourceFactory extends PadlSourceFactory {

    @Override
    public DatabaseSource getInstance() {
        return new DatabaseSource();
    }

}
