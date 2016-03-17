package me.chyxion.dao.pagination;

import java.sql.Connection;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion
 */
public interface PaginationProcessorProvider {
	
	/**
	 * @param conn data base connection
	 * @return pagination processor
	 */
	PaginationProcessor create(Connection conn);
}
