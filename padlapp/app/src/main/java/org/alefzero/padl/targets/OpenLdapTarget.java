package org.alefzero.padl.targets;

/**
 * Creates a target to manipulate an openldap used as internal engine for padl.
 */
public class OpenLdapTarget implements PadlTarget {

    private static final String ID = "openldap";

    @Override
    public String getId() {
        return ID;
    }

}
