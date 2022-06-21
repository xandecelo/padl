package org.alefzero.padl.targets;

/**
 * Factory of openldap padl target objects.
 */
public class OpenLdapTargetFactory implements PadlTargetFactory {

    @Override
    public PadlTarget getInstance() {
        return new OpenLdapTarget();
    }
}
