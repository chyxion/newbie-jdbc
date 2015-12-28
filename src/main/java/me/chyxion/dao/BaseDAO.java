package me.chyxion.dao;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Mar 28, 2012 2:30:19 PM
 */
public interface BaseDAO extends BasicDAO {

	/**
	 * execute connection operation
	 * @param co
	 * @return
	 */
	<T> T execute(Co<T> co);

	/**
	 * @param co
	 * @return
	 */
	<T> T executeTransaction(Co<T> co);
}
