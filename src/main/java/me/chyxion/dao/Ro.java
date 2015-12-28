package me.chyxion.dao;

import java.sql.ResultSet;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 20, 2015 4:53:27 PM
 */
public interface Ro<T> {
	T run(ResultSet rs) throws Throwable;
}
