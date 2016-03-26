package me.chyxion.jdbc;

import java.util.Map;
import java.util.List;
import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion
 */
interface BasicJdbc {

	/**
	 * find single result
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @param <T> result type
	 * @return query result
	 */
	<T> T findOne(Ro<T> ro, String sql, Object... args); 

	/**
	 * find single value, a string or int etc.
	 * <code>
	 * 		long count = findValue("select count(1) from users");	
	 * </code>
	 * @param sql sql
	 * @param args sql args
	 * @param <T> result type
	 * @return query result
	 */
	<T> T findValue(String sql, Object... args); 
	
	/**
	 * list single column values, string or int etc.
	 * <code>
	 * 		List&lt;String&gt; names = listValue("select name from users");	
	 * </code>
	 * @param sql sql
	 * @param args sql args
	 * @param <T> result type
	 * @return query result
	 */
	<T> List<T> listValue(String sql, Object... args);

	/**
	 * list by sql
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @param <T> result type
	 * @return query result
	 */
	<T> List<T> list(Ro<T> ro, String sql, Object... args); 
	
	/**
	 * query by sql
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @param <T> result type
	 * @return query result
	 */
	<T> T query(Ro<T> ro, String sql, Object... args); 

	/**
	 * execute sql
	 * @param sql sql
	 * @param args sql args
	 * @return execute result
	 */
	boolean execute(String sql, Object... args); 
	
	/**
	 * execute batch
	 * @param sql sql
	 * @param args args
	 * @param batchSize batch size
	 * @return effect rows result
	 */
	int executeBatch(String sql, int batchSize, Collection<?>... args); 
	
	/**
	 * execute batch
	 * @param sql sql
	 * @param batchSize batch size
	 * @param args sql args
	 * @return effect rows result
	 */
	int executeBatch(String sql, 
			int batchSize, Collection<Collection<?>> args); 
	
	/**
	 * insert multiple rows
	 * @param table table
	 * @param cols column names
	 * @param args rows data
	 * @param batchSize batch size
	 * @return effect rows result
	 */
	int insert(String table, 
			Collection<String> cols, 
			Collection<Collection<?>> args, 
			int batchSize); 

	/**
	 * insert single row
	 * @param table table
	 * @param data row data
	 * @return effect rows result
	 */
	int insert(String table, Map<String, ?> data); 
	
	/**
	 * update by sql and args
	 * @param sql sql
	 * @param args sql args
	 * @return effect rows result
	 */
	int update(String sql, final Object... args); 
	
	/**
	 * query map list
	 * @param sql sql
	 * @param args args
	 * @return query result
	 */
	List<Map<String, Object>> listMap(String sql, Object... args); 
	
	/**
	 * list map page
	 * @param sql sql
	 * @param orders orders
	 * @param offset row offset
	 * @param limit row limit
	 * @param args sql args
	 * @return query result
	 */
	List<Map<String, Object>> listMapPage(
		String sql, 
		Collection<Order> orders, 
		int offset, 
		int limit, 
		Object... args);
	
	/**
	 * find single map
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	Map<String, Object> findMap(String sql, Object... args); 
}
