package me.chyxion.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Mar 20, 2016 12:05:36 PM
 */
public interface Ro<T> {
	
	/**
	 * @param rs result set
	 * @return execute result
	 * @throws SQLException
	 */
	T exec(ResultSet rs) throws SQLException;
}
