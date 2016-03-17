package me.chyxion.dao;

import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import java.sql.Connection;
import javax.sql.DataSource;
import java.util.Collection;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import me.chyxion.dao.pagination.PaginationProcessorProvider;
import me.chyxion.dao.pagination.DefaultPaginationProcessorProvider;

/**
 * @version 0.0.1
 * @since 0.0.1 
 * @author Shaun Chyxion
 */
public final class BaseDAOSupport implements BaseDAO {
	private static final Logger log = 
		LoggerFactory.getLogger(BaseDAOSupport.class);
	private DataSource dataSource;
	private PaginationProcessorProvider paginationProcessorProvider;
	private ResultSetReader resultSetReader;

	/**
	 * @param dataSource database data source
	 */
	public BaseDAOSupport(DataSource dataSource) {
		this(dataSource, new DefaultPaginationProcessorProvider());
	}

	/**
	 * @param dataSource database data source
	 * @param paginationProcessorProvider pagination processor provider
	 */
	public BaseDAOSupport(DataSource dataSource, 
			PaginationProcessorProvider paginationProcessorProvider) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
				"Data Source Could Not Be Null");
		}
		this.dataSource = dataSource;

		if (paginationProcessorProvider == null) {
			throw new IllegalArgumentException(
				"Pagination Processor Provider Source Could Not Be Null");
		}
		this.paginationProcessorProvider = paginationProcessorProvider;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the paginationProcessorProvider
	 */
	public PaginationProcessorProvider getPaginationProcessorProvider() {
		return paginationProcessorProvider;
	}

	/**
	 * @param paginationProcessorProvider the paginationProcessorProvider to set
	 */
	public void setPaginationProcessorProvider(
			PaginationProcessorProvider paginationProcessorProvider) {
		this.paginationProcessorProvider = paginationProcessorProvider;
	}

	/**
	 * @return the resultSetReader
	 */
	public ResultSetReader getResultSetReader() {
		return resultSetReader;
	}

	/**
	 * @param resultSetReader the resultSetReader to set
	 */
	public void setResultSetReader(ResultSetReader resultSetReader) {
		this.resultSetReader = resultSetReader;
	}
	/**
	 * {@inheritDoc}
	 */
	public <T> T findValue(final String sql, final Object ... values) {
		return execute(new Co<T>() {
			@Override
			protected T run() {
				return findValue(sql, values);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findValue(Connection conn, 
		String sql, Object ... values) {
		return bd(conn).findValue(sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T query(final Ro<T> rso, 
		final String strSQL, final Object ... values) {
		return execute(new Co<T>() {
			@Override
			protected T run() {
				return query(rso, strSQL, values);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T query(Connection conn, 
		Ro<T> rso, String sql, Object ... values) {
		return bd(conn).query(rso, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T execute(Co<T> co) {
		Connection conn = null;
		try {
			conn = getConnection();
            co.conn = conn;
            co.ppp = paginationProcessorProvider;
            co.resultSetReader = resultSetReader;
			return co.run();
		} 
		catch (SQLException e) {
			log.error("Execute Connection Operation Error Caused.", e);
			throw new IllegalStateException(e);
		} 
		finally {
			close(conn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean execute(Connection conn, String sql, Object... args) {
		return bd(conn).execute(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean execute(final String sql, 
		final Object... args) {
		return execute(new Co<Boolean>() {
			@Override
			protected Boolean run() {
				return execute(sql, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	public int executeBatch(final String sql, 
		final int batchSize, final List<?>... args) {
		return executeTransaction(new Co<Integer>() {
			@Override
			protected Integer run() {
				return executeBatch(sql, batchSize, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(Connection conn, 
		String strSQL, int batchSize, List<?>... args) {
		return bd(conn).executeBatch(strSQL, batchSize, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(Connection conn, 
		String table, List<String> cols, 
		Collection<List<?>> args, int batchSize) {
		return bd(conn).insert(table, cols, args, batchSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(Connection conn, String table, Map<String, ?> data) {
		return bd(conn).insert(table, data);
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(final String table, 
		final List<String> cols, 
		final Collection<List<?>> args, final int batchSize) {
		return execute(new Co<Integer>() {
			@Override
			protected Integer run() {
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
			protected Integer run() {
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

	/**
	 * {@inheritDoc}
	 */
	public int update(Connection conn, 
		final String sql, final Object... args) {
		return bd(conn).update(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMapPage(
		Connection conn, 
		String sql, 
		List<Order> orders,
		int start,
		int limit,
		final Object ... args) {
		return bd(conn).listMapPage(sql, orders, start, limit, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMapPage(
		final String sql, 
		final List<Order> orders,
		final int start,
		final int limit,
		final Object... args) {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run() {
				return listMapPage(sql, orders, start, limit, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMap(
		Connection conn, 
		final String sql, 
		final Object ... args) {
		return bd(conn).listMap(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMap(
		final String sql, final Object... args) {
		return execute(new Co<List<Map<String, Object>>>() {
			@Override
			protected List<Map<String, Object>> run() {
				return listMap(sql, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
		Connection conn, 
		final String sql, 
		final Object ... args) {
		return bd(conn).findMap(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
		final String sql, 
		final Object ... args) {
		return execute(new Co<Map<String, Object>>() {
			@Override
			protected Map<String, Object> run() {
				return findMap(sql, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findOne(final Ro<T> ro, final String sql, final Object... args) {
		return execute(new Co<T>() {
			@Override
			protected T run() {
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
			protected List<T> run() {
				return list(ro, sql, args);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findOne(Connection conn, 
		Ro<T> ro, String sql, Object... args) {
		return bd(conn).findOne(ro, sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> list(Connection conn, 
		Ro<T> ro, String sql, Object... args) {
		return bd(conn).list(ro, sql, args);
	}

	// --
	// private methods

	private Connection getConnection() {
		try {
			return dataSource.getConnection();
		}
		catch (SQLException e) {
			throw new IllegalStateException(
				"Get Database Connection Error Caused", e);
		}
	}

	private void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			}
			catch (SQLException e) {
				log.warn("Close Connection Error Caused.", e);
			}
		}
	}

	private BasicDAO bd(Connection conn) {
		return new BasicDAOSupport(conn, 
					paginationProcessorProvider, resultSetReader);
	}
}
