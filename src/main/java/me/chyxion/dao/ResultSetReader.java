package me.chyxion.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion
 */
public interface ResultSetReader {
	
	/**
	 * read result set
	 * @param rs result set
	 * @param index data index
	 * @param type data type
	 * @return result
	 * @throws SQLException SQL error caused
	 */
	Object read(ResultSet rs, int index, int type) throws SQLException;
}
