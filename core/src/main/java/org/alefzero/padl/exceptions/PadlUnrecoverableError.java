package org.alefzero.padl.exceptions;

public class PadlUnrecoverableError extends Error {

	private static final long serialVersionUID = -8169155289909088853L;

	public PadlUnrecoverableError() {
		super();
	}

	public PadlUnrecoverableError(String message) {
		super(message);
	}

	public PadlUnrecoverableError(String message, Throwable cause) {
		super(message, cause);
	}

	public PadlUnrecoverableError(Throwable cause) {
		super(cause);
	}
}
