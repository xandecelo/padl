package org.alefzero.padl.targets;

import java.lang.invoke.MethodHandles;

import org.alefzero.padl.core.services.PadlTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a target to manipulate an openldap used as internal engine for padl.
 */
public class OpenLdapTarget extends PadlTarget {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "openldap";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected String getRootCN() {
        return "cn=admin,cn=config";
    }

}
