package me.chyxion.dao;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 20, 2015 4:53:34 PM
 */
public abstract class Co<T> extends DAOCore {
	protected abstract T run() throws Throwable;
}