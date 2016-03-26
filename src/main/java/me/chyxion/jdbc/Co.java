package me.chyxion.jdbc;

import java.sql.SQLException;

/**
 * connection executor
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 20, 2015 4:53:34 PM
 */
public abstract class Co<T> extends BasicJdbcSupport {
	
	/**
	 * @return execute result
	 * @throws SQLException sql exception caused
	 */
	protected abstract T run() throws SQLException;
}
