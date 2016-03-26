package me.chyxion.jdbc.pagination;

import java.util.Collection;

import me.chyxion.jdbc.Order;
import me.chyxion.jdbc.SqlAndArgs;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 10, 2015 10:08:05 PM
 */
public interface PaginationProcessor {
	/**
	 * row number for pagination
	 */
	String COLUMN_ROW_NUMBER = "row_number__";

	/**
	 * @param orders query result orders
	 * @param offset row offset
	 * @param limit row limit
	 * @param sql sql
	 * @param args sql args
	 * @return sql and args object
	 */
    SqlAndArgs process(
    		Collection<Order> orders,
    		int offset, 
    		int limit,
    		String sql, 
    		Collection<? super Object> args);
}
