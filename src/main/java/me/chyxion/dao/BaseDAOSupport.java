package me.chyxion.dao;

import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import java.sql.Statement;
import java.sql.Connection;
import javax.sql.DataSource;
import java.util.Collection;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Mar 28, 2012 2:30:19 PM
 */
public final class BaseDAOSupport implements BaseDAO {
	private static final Logger log = 
		LoggerFactory.getLogger(BaseDAOSupport.class);
	private DataSource dataSource;

	/**
	 * SQL batch size
	 */
	public static int batchSize = 1024;

	/**
	 * 获得连接
	 */
	public Connection getConnection()  {
		try {
			return dataSource.getConnection();
		}
		catch (SQLException e) {
			throw new IllegalStateException(
				"Get Connection Error Caused", e);
		}
	}

	public <T> T findValue(final String strSQL, final Object ... values)  {
		return execute(new Co<T>() {
			@Override
			protected T run()  {
				return findValue(strSQL, values);
			}
		});
	}

	public <T> T findValue(Connection dbConnection, String strSQL, Object ... values)  {
		return new DAOCore(dbConnection).findValue(strSQL, values);
	}

	public <T> List<T> findValueList(Connection dbConnection, String strSQL, final Object ... values) {
		return new DAOCore(dbConnection).listValue(strSQL, values);
	}

	public <T> List<T> listValue(final String strSQL, final Object ... values) {
		return execute(new Co<List<T>>() {
			@Override
			protected List<T> run()  {
				return listValue(strSQL, values);
			}
		});
	}

	/**
	 * @param strSQL
	 * @param values
	 * @param rso
	 * @return
	 */
	public <T> T query(final Ro<T> rso, final String strSQL, final Object ... values)  {
		return execute(new Co<T>() {
			@Override
			protected T run() {
				return query(rso, strSQL, values);
			}
		});
	}

	/**
	 * @param dbConnection
	 * @param sql
	 * @param values
	 * @param rso
	 * @return
	 */
	public <T> T query(Connection dbConnection, Ro<T> rso, String sql, Object ... values)  {
		return new DAOCore(dbConnection).query(rso, sql, values);
	}

	/**
	 * 执行Connection的操作, 参数为Connection操作器,
	 * 注意，该操作不带事物，只是使用同一个连接，如需执行事务，请使用executeTransaction
	 * @param co
	 */
	public <T> T execute(Co<T> co)  {
		Connection dbConnection = null;
		try {
			dbConnection = getConnection();
            co.conn = dbConnection;
			return co.run();
		} 
		catch (Throwable e) {
			log.error("Execute Connection Operation Error Caused", e);
			throw new IllegalStateException(e);
		} 
		finally {
			// close(co.preparedStatement);
			close(dbConnection);
		}
	}

	public boolean execute(Connection dbConnection, String strSQL)  {
		return new DAOCore(dbConnection).execute(strSQL);
	}

	public boolean execute(final String strSQL)  {
		return execute(new Co<Boolean>() {
			@Override
			protected Boolean run()  {
				return execute(strSQL);
			}
		});
	}

	/**
	 * 执行事务
	 * @param co
	 * @return
	 */
	public <T> T executeTransaction(Co<T> co) {
		Connection conn = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
            co.conn = conn;
			T t = co.run();
			conn.commit();
			return t;
		} 
		catch (Throwable e) {
			log.error("Execute Transaction Error Caused", e);
			try {
				conn.rollback();
			} 
			catch (SQLException se) {
				log.error("Execute Transaction Rollback Error Caused", e);
				throw new IllegalStateException(se);
			}
			throw new IllegalStateException(e);
		} 
		finally {
			// close(co.preparedStatement);
			close(conn);
		}
	}

	/**
	 * 批量执行
	 * @param listSQLs
	 * @return 更新的数据行数 数组。
	 */
	public void executeBatch(final Collection<String> listSQLs)  {
		executeTransaction(new Co<Object>() {
			@Override
			protected Object run()  {
				executeBatch(listSQLs, 16);
				return null;
			}
		});
	}

	/**
	 * @param dbConnection
	 * @param sqls
	 */
	public void executeBatch(Connection dbConnection, Collection<String> sqls) {
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			int i = 0;
			for (String sql : sqls) {
				statement.addBatch(sql);
				if (++i > batchSize) {
					i = 0;
					statement.executeBatch();
				}
			}
			statement.executeBatch();
		} 
		catch (SQLException e) {
			log.error("Execute Batch Error Caused", e);
			throw new RuntimeException(e);
		} 
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					log.warn("Statement Close Error Caused", e);
				}
			}
		}
	}

	/**
	 * 执行批SQL
	 * @param strSQL, insert into foobar (?, ?)
	 * @param jaValues, [[0, 1], [2, 3]]
	 * @
	 */
	public int executeBatch(final String sql, final Collection<List<?>> args, final int batchSize) {
		return executeTransaction(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return executeBatch(sql, args, batchSize);
			}
		});
	}

	/**
	 * 执行批SQL
	 * @param dbConnection
	 * @param strSQL, insert into foobar (?, ?)
	 * @param jaValues, [[1, 2], [3, 4]]
	 * @
	 */
	public void executeBatch(Connection dbConnection, String strSQL, Collection<List<?>> args, int batchSize)  {
		new DAOCore(dbConnection).executeBatch(strSQL, args, batchSize);
	}

	public void insert(Connection dbConnection, String table, List<String> cols, Collection<List<?>> args, int batchSize)  {
		new DAOCore(dbConnection).insert(table, cols, args, batchSize);
	}

	public int insert(final String table, final List<String> cols, final Collection<List<?>> args, final int batchSize)  {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return insert(table, cols, args, batchSize);
			}
		});
	}

	/**
	 * @param strSQL
	 * @param values 
	 * @return
	 * @
	 */
	public int update(final String strSQL, final Object ... values) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return update(strSQL, values);
			}
		});
	}

	public int update(Connection dbConnection, final String strSQL, final Object ... values) {
		return new DAOCore(dbConnection).update(strSQL, values);
	}

	public List<Map<String, Object>> findMapList(
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return new DAOCore(dbConnection).listMap(strSQL, values);
	}

	public List<Map<String, Object>> 
		findMapListPage(
					Connection dbConnection, 
					List<Order> orders,
					int start,
					int limit,
					String strSQL, 
					final Object ... values)  {
		return new DAOCore(dbConnection).listMapPage(orders, start, limit, strSQL, values);
	}

	public List<Map<String, Object>> 
		listMapPage(
			final List<Order> orders,
			final int start,
			final int limit,
			final String strSQL, 
			final Object ... values)  {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run()  {
				return listMapPage(orders, start, limit, strSQL, values);
			}
		});
	}

	public List<Map<String, Object>> listMap(
		final String strSQL, final Object ... values) {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run()  {
				return listMap(strSQL, values);
			}
		});
	}

	public Map<String, Object> findMap(
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return new DAOCore(dbConnection).findMap(strSQL, values);
	}

	public Map<String, Object> findMap(final String strSQL, 
			final Object ... values) {
		return execute(new Co<Map<String, Object>>() {
			@Override
			protected Map<String, Object> run()  {
				return findMap(strSQL, values);
			}
		});
	}

	private void close(Connection connection) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(final Collection<String> sqls, final int batchSize) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return executeBatch(sqls, batchSize);
			}
		});
	}
}
