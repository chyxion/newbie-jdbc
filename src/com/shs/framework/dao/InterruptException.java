package com.shs.framework.dao;

public class InterruptException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InterruptException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InterruptException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterruptException(String message) {
		super(message);
	}

	public InterruptException(Throwable cause) {
		super(cause);
	}
}
