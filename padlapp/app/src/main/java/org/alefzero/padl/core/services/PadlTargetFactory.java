package org.alefzero.padl.core.services;

/**
 * Factory interface of padl target objects
 */
public abstract class PadlTargetFactory implements GenericServiceFactory {
    public abstract PadlTarget getInstance();
}
