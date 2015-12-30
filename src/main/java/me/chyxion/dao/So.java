package me.chyxion.dao;

import java.sql.Statement;
import java.sql.SQLException;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 29, 2015 8:12:31 PM
 */
public interface So<T> {
	Statement build() throws SQLException;
	T exec(Statement statement) throws SQLException;
}
