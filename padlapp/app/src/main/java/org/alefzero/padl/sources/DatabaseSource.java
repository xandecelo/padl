package org.alefzero.padl.sources;

import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.apache.directory.api.ldap.model.entry.Entry;

/**
 * A LdapSource is used to get data from an LDAP X.500 compliant implementation
 * according with provided configuration.
 */
public class DatabaseSource extends PadlSource {

    private static final String ID = "database";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        // TODO Auto-generated method stub
        return new LinkedList<Entry>();
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PadlTarget getTarget() {
        // TODO Auto-generated method stub
        return null;
    }


}
