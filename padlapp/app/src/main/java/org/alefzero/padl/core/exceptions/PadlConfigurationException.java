package org.alefzero.padl.core.exceptions;

/**
 * Exception at configuration phase of the target ldap.
 */
public class PadlConfigurationException extends PadlException {

    public PadlConfigurationException() {
    }

    public PadlConfigurationException(String message) {
        super(message);
    }

    public PadlConfigurationException(Throwable cause) {
        super(cause);
    }

    public PadlConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PadlConfigurationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
