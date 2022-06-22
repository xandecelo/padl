package org.alefzero.padl.targets;

import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.core.services.PadlTargetFactory;

/**
 * Factory of openldap padl target objects.
 */
public class OpenLdapTargetFactory extends PadlTargetFactory {

    @Override
    public PadlTarget getInstance() {
        return new OpenLdapTarget();
    }
}
