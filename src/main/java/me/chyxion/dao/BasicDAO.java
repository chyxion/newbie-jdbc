package me.chyxion.dao;

import java.util.Map;
import java.util.List;
import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion
 */
interface BasicDAO {

	/**
	 * find single result
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
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
	 * @return query result
	 */
	<T> T findValue(String sql, Object... args); 
	
	/**
	 * list by sql
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> List<T> list(Ro<T> ro, String sql, Object... args); 
	
	/**
	 * query by sql
	 * @param ro result set operator
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	<T> T query(Ro<T> ro, String sql, Object... args); 

	/**
	 * execute single sql
	 * @param sql sql args
	 * @return execute result
	 */
	boolean execute(String sql, Object... args); 
	
	/**
	 * execute batch
	 * @param sql sql
	 * @param args args
	 * @param batchSize batch size
	 * @return execute result
	 */
	int executeBatch(String sql, int batchSize, List<?>... args); 
	
	/**
	 * insert multiple rows
	 * @param table table
	 * @param cols column names
	 * @param args rows data
	 * @param batchSize batch size
	 * @return insert result
	 */
	int insert(String table, List<String> cols, Collection<List<?>> args, int batchSize); 

	/**
	 * insert single row
	 * @param table table
	 * @param data row data
	 * @return insert result
	 */
	int insert(String table, Map<String, ?> data); 
	
	/**
	 * update by sql and args
	 * @param sql sql
	 * @param args sql args
	 * @return update result
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
	 * @param orders orders
	 * @param offset row offset
	 * @param limit row limit
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	List<Map<String, Object>> listMapPage(
		List<Order> orders, 
		int offset, 
		int limit, 
		String sql, 
		Object... args);
	
	/**
	 * find single map
	 * @param sql sql
	 * @param args sql args
	 * @return query result
	 */
	Map<String, Object> findMap(String sql, Object... args); 
}
