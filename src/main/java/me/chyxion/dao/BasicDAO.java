package me.chyxion.dao;

import java.util.Map;
import java.util.List;
import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Mar 28, 2012 2:30:19 PM
 */
interface BasicDAO {

	/**
	 * list values
	 * @param sql
	 * @param args
	 * @return
	 */
	<T> List<T> listValue(String sql, Object... args); 
	
	/**
	 * @param sql
	 * @param args
	 * @return
	 */
	<T> T findValue(String sql, Object... args); 
	
	/**
	 * query
	 * @param rso
	 * @param sql
	 * @param args
	 * @return
	 */
	<T> T query(Ro<T> rso, String sql, Object... args); 

	/**
	 * @param co
	 * @return
	 */
	// <T> T execute(Co<T> co);

	/**
	 * execute sql
	 * @param sql
	 * @return
	 */
	boolean execute(String sql); 
	
	/**
	 * execute batch
	 * @param sqls
	 * @param batchSize
	 * @return
	 */
	int executeBatch(Collection<String> sqls, int batchSize); 
	
	/**
	 * execute batch
	 * @param sql
	 * @param args
	 * @param batchSize
	 * @return
	 */
	int executeBatch(String sql, Collection<List<?>> args, int batchSize); 
	
	/**
	 * insert 
	 * @param table
	 * @param cols
	 * @param args
	 * @param batchSize
	 * @return
	 */
	int insert(String table, List<String> cols, Collection<List<?>> args, int batchSize); 
	
	/**
	 * update
	 * @param sql
	 * @param args
	 * @return
	 */
	int update(String sql, final Object... args); 
	
	/**
	 * list map
	 * @param sql
	 * @param args
	 * @return
	 */
	List<Map<String, Object>> listMap(String sql, Object... args); 
	
	/**
	 * list map page
	 * @param orders
	 * @param offset
	 * @param limit
	 * @param sql
	 * @param args
	 * @return
	 */
	List<Map<String, Object>> listMapPage(
		List<Order> orders, 
		int offset, 
		int limit, 
		String sql, 
		Object... args);
	
	/**
	 * find map
	 * @param sql
	 * @param args
	 * @return
	 */
	Map<String, Object> findMap(String sql, Object... args); 
}
