package org.alefzero.padl.core.exceptions;

/**
 * General exception for padl operations
 */
public class PadlException extends Exception {

    public PadlException() {
    }

    public PadlException(String message) {
        super(message);
    }

    public PadlException(Throwable cause) {
        super(cause);
    }

    public PadlException(String message, Throwable cause) {
        super(message, cause);
    }

    public PadlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
   
    
}
