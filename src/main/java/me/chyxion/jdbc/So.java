package me.chyxion.jdbc;

import java.sql.Statement;
import java.sql.SQLException;

/**
 * SQL statement executor
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 29, 2015 8:12:31 PM
 */
public interface So<T> {
	
	/**
	 * build statement
	 * @return sql statement
	 * @throws SQLException sql exception caused
	 */
	Statement build() throws SQLException;
	
	/**
	 * execute statement
	 * @param statement sql statement
	 * @return execute result
	 * @throws SQLException sql exception caused
	 */
	T exec(Statement statement) throws SQLException;
}
