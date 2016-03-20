package me.chyxion.jdbc;

import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Mar 28, 2012 2:30:19 PM
 */
public interface NewbieJdbc extends BasicJdbc {

	/**
	 * execute connection operator, no transaction
	 * @param co connection operator
	 * @return execute result
	 */
	<T> T execute(Co<T> co);

	/**
	 * execute connection operator with transaction
	 * @param co connection operator
	 * @return execute result
	 */
	<T> T executeTransaction(Co<T> co);
	
	
	/**
	 * find single result
	 * @param conn db connection
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> T findOne(Connection conn, Ro<T> ro, String sql, Object... args); 

	/**
	 * find single value, a string or int etc.
	 * <code>
	 * 		long count = findValue("select count(1) from users");	
	 * </code>
	 * @param conn db connection
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> T findValue(Connection conn, String sql, Object... args); 
	
	/**
	 * list single column values, string or int etc.
	 * <code>
	 * 		List<String> names = listValue("select name from users");	
	 * </code>
	 * @param conn db connection
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> List<T> listValue(Connection conn, String sql, Object... values);

	/**
	 * list by sql
	 * @param conn db connection
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> List<T> list(Connection conn, Ro<T> ro, String sql, Object... args); 
	
	/**
	 * query by sql
	 * @param conn db connection
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> T query(Connection conn, Ro<T> ro, String sql, Object... args); 

	/**
	 * execute single sql
	 * @param conn db connection
	 * @param sql sql args
	 * @return execute result
	 */
	boolean execute(Connection conn, String sql, Object... args); 
	
	/**
	 * execute batch
	 * @param conn db connection
	 * @param sql sql
	 * @param args args
	 * @param batchSize batch size
	 * @return execute result
	 */
	int executeBatch(Connection conn, 
			String sql, int batchSize, Collection<?>... args); 
	
	/**
	 * execute batch
	 * @param conn db connection
	 * @param sql sql
	 * @param args args
	 * @param batchSize batch size
	 * @return execute result
	 */
	int executeBatch(Connection conn, 
			String sql, int batchSize, Collection<Collection<?>> args); 
	
	/**
	 * insert multiple rows
	 * @param conn db connection
	 * @param table table
	 * @param cols column names
	 * @param args rows data
	 * @param batchSize batch size
	 * @return insert result
	 */
	int insert(Connection conn, 
			String table, 
			Collection<String> cols, 
			Collection<Collection<?>> args, 
			int batchSize); 

	/**
	 * insert single row
	 * @param conn db connection
	 * @param table table
	 * @param data row data
	 * @return insert result
	 */
	int insert(Connection conn, String table, Map<String, ?> data); 
	
	/**
	 * update by sql and args
	 * @param conn db connection
	 * @param sql sql
	 * @param args sql args
	 * @return update result
	 */
	int update(Connection conn, String sql, final Object... args); 
	
	/**
	 * query map list
	 * @param conn db connection
	 * @param sql sql
	 * @param args args
	 * @return query result
	 */
	List<Map<String, Object>> listMap(Connection conn, 
			String sql, Object... args); 
	
	/**
	 * list map page
	 * @param conn db connection
	 * @param sql sql
	 * @param orders orders
	 * @param offset row offset
	 * @param limit row limit
	 * @param args sql args
	 * @return query result
	 */
	List<Map<String, Object>> listMapPage(
			Connection conn,
			String sql, 
			Collection<Order> orders, 
			int offset, 
			int limit, 
			Object... args);
	
	/**
	 * find single map
	 * @param conn db connection
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	Map<String, Object> findMap(Connection conn, String sql, Object... args); 
		
}
