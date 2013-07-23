package com.shs.framework.dao;
/**
 * @version 0.1
 * @author chyxion
 * @describe: 中断异常，是dao前置事件中返回false，将抛出中断异常
 * @date created: Jul 23, 2013 10:30:46 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by:
 */
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
