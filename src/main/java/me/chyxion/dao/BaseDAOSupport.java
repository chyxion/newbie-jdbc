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
	 * @param dataSource
	 */
	public BaseDAOSupport(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
				"Data Source Could Not Be Null");
		}
		this.dataSource = dataSource;
	}

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

	public <T> T findValue(Connection conn, String strSQL, Object ... values)  {
		return new DAOCore(conn).findValue(strSQL, values);
	}

	public <T> List<T> findValueList(Connection conn, String strSQL, final Object ... values) {
		return new DAOCore(conn).listValue(strSQL, values);
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

	public <T> T query(Connection conn, Ro<T> rso, String sql, Object ... values)  {
		return new DAOCore(conn).query(rso, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T execute(Co<T> co)  {
		Connection conn = null;
		try {
			conn = getConnection();
            co.conn = conn;
			return co.run();
		} 
		catch (SQLException e) {
			log.error("Execute Connection Operation Error Caused", e);
			throw new IllegalStateException(e);
		} 
		finally {
			// close(co.preparedStatement);
			close(conn);
		}
	}

	public boolean execute(Connection conn, String strSQL)  {
		return new DAOCore(conn).execute(strSQL);
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
		catch (SQLException e) {
			log.error("Execute Transaction Error Caused.", e);
			try {
				conn.rollback();
			} 
			catch (SQLException se) {
				log.error("Execute Transaction Rollback Error Caused.", e);
				throw new IllegalStateException(se);
			}
			throw new IllegalStateException(e);
		} 
		finally {
			close(conn);
		}
	}

	/**
	 * @param conn
	 * @param sqls
	 */
	public void executeBatch(Connection conn, Collection<String> sqls, int batchSize) {
		Statement statement = null;
		try {
			statement = conn.createStatement();
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
			log.error("Execute Batch Error Caused.", e);
			throw new IllegalStateException(e);
		} 
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					log.warn("Statement Close Error Caused.", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
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
	 * @param conn
	 * @param strSQL, insert into foobar (?, ?)
	 * @param jaValues, [[1, 2], [3, 4]]
	 * @
	 */
	public void executeBatch(Connection conn, String strSQL, Collection<List<?>> args, int batchSize)  {
		new DAOCore(conn).executeBatch(strSQL, args, batchSize);
	}

	public int insert(Connection conn, String table, List<String> cols, Collection<List<?>> args, int batchSize) {
		return new DAOCore(conn).insert(table, cols, args, batchSize);
	}

	public int insert(Connection conn, String table, Map<String, ?> data) {
		return new DAOCore(conn).insert(table, data);
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(final String table, final List<String> cols, final Collection<List<?>> args, final int batchSize) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return insert(table, cols, args, batchSize);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(final String table, final Map<String, ?> data) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run()  {
				return insert(table, data);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public int update(final String sql, final Object... args) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run() {
				return update(sql, args);
			}
		});
	}

	public int update(Connection conn, final String sql, final Object... args) {
		return new DAOCore(conn).update(sql, args);
	}

	public List<Map<String, Object>> findMapList(
			Connection conn, 
			final String sql, 
			final Object ... args)  {
		return new DAOCore(conn).listMap(sql, args);
	}

	public List<Map<String, Object>> 
		findMapListPage(
			Connection conn, 
			List<Order> orders,
			int start,
			int limit,
			String sql, 
			final Object ... args)  {
		return new DAOCore(conn).listMapPage(orders, start, limit, sql, args);
	}

	public List<Map<String, Object>> 
		listMapPage(
			final List<Order> orders,
			final int start,
			final int limit,
			final String sql, 
			final Object... args)  {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run()  {
				return listMapPage(orders, start, limit, sql, args);
			}
		});
	}

	public List<Map<String, Object>> listMap(
		final String sql, final Object... args) {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run()  {
				return listMap(sql, args);
			}
		});
	}

	public Map<String, Object> findMap(
			Connection conn, 
			final String sql, 
			final Object ... args)  {
		return new DAOCore(conn).findMap(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
		final String sql, 
		final Object ... args) {
		return execute(new Co<Map<String, Object>>() {
			@Override
			protected Map<String, Object> run()  {
				return findMap(sql, args);
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

	/**
	 * {@inheritDoc}
	 */
	public <T> T findOne(final Ro<T> ro, final String sql, final Object... args) {
		return execute(new Co<T>() {
			@Override
			protected T run()  {
				return findOne(ro, sql, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> list(
		final Ro<T> ro, final String sql, final Object... args) {
		return execute(new Co<List<T>>() {
			@Override
			protected List<T> run()  {
				return list(ro, sql, args);
			}
		});
	}
}
