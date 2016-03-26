package me.chyxion.jdbc;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import me.chyxion.jdbc.pagination.PaginationProcessor;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Mar 25, 2016 9:10:57 PM
 */
public interface CustomResolver {
	
	/**
	 * get pagination processor 
	 * @param conn database connection
	 * @return pagination processor 
	 */
	PaginationProcessor getPaginationProcessor(Connection conn);

	/**
	 * set prepared statement value
	 * @param ps prepared statement 
	 * @param index param index
	 * @param param param
	 * @throws SQLException
	 */
	void setParam(PreparedStatement ps, 
			int index, Object param) throws SQLException;
	
	/**
	 * read result set value
	 * @param rs result set
	 * @param index value index
	 * @return value
	 * @throws SQLException
	 */
	Object readValue(ResultSet rs, int index) throws SQLException;
}
