package me.chyxion.jdbc;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.sql.SQLException;
import java.lang.reflect.Array;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;

import org.slf4j.helpers.MessageFormatter;

import me.chyxion.jdbc.pagination.PaginationProcessor;
import me.chyxion.jdbc.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 20, 2015 5:28:00 PM
 */
class BasicJdbcSupport implements BasicJdbc {
	private static final Logger log = 
		LoggerFactory.getLogger(BasicJdbcSupport.class);

	Connection conn;
	CustomResolver customResolver;

	/**
	 * @param onn database connection
	 * @param customResolver custom resolver
	 */
	public BasicJdbcSupport(Connection conn, 
			CustomResolver customResolver) {
		this.conn = conn;
		this.customResolver = customResolver;
	}
	
	/**
	 * default constructor
	 */
	public BasicJdbcSupport() {}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> listValue(final String sql, Object... values) {
		return list(new Ro<T>() {
			@SuppressWarnings("unchecked")
			public T exec(ResultSet rs) throws SQLException {
				if (rs.getMetaData().getColumnCount() > 1) {
					throw new IllegalStateException(fmt(
						"List Values By SQL [{}] Expected One Column To Be Returned, " + 
						"But Found More Than One", sql));
				}
				return (T) customResolver.readValue(rs, 1);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findValue(final String sql, Object... values) {
		return findOne(new Ro<T>() {
			@SuppressWarnings("unchecked")
			public T exec(ResultSet rs) throws SQLException {
				if (rs.getMetaData().getColumnCount() > 1) {
					throw new IllegalStateException(fmt(
						"Find Value By SQL [{}] Expected One Column To Be Returned, " + 
						"But Found More Than One", sql));
				}
				return (T) customResolver.readValue(rs, 1);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T query(final Ro<T> rso, 
			final String sql, final Object ... args) {
		return exec(new So<T>() {
			public Statement build() throws SQLException {
				return prepareStatement(conn, sql, Arrays.asList(args));
			}

			public T exec(Statement statement) throws SQLException {
				ResultSet rs = null;
				try {
					rs = ps(statement).executeQuery();
					return rso.exec(rs);
				}
				finally {
					if (rs != null) {
						try {
							rs.close();
						} 
						catch (SQLException e) {
							log.warn("Result Set Close Error Cause", e);
						}
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean execute(final String sql, final Object... args) {
		return exec(new So<Boolean>() {
			public Statement build() throws SQLException {
				return prepareStatement(conn, sql, Arrays.asList(args));
			}

			public Boolean exec(Statement statement) throws SQLException {
				return ps(statement).execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(final String sql, 
			final int batchSize,
			final Collection<?>... args) {
		log.info("execute batch[{}]", sql);
		return exec(new So<Integer>() {
			
			/**
			 * {@inheritDoc}
			 */
			public Statement build() throws SQLException {
				return conn.prepareStatement(sql);
			}

			/**
			 * {@inheritDoc}
			 */
			public Integer exec(Statement statement) throws SQLException {
				int result = 0;
				int i = 0;
				int bs = batchSize > 0 ? batchSize : 16;
				for (Collection<?> list : args) {
					setValues(ps(statement), list);
					ps(statement).addBatch();
					if (++i % bs == 0 && i != 0) {
						result += sum(statement.executeBatch());
					}
				}
				result += sum(statement.executeBatch());
				return result;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(final String sql, 
			final int batchSize,
			final Collection<Collection<?>> args) {
		return executeBatch(sql, batchSize, args.toArray(new Collection<?>[0]));
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(String table, 
			Collection<String> cols, 
			Collection<Collection<?>> values, 
			int batchSize) {
		return executeBatch(genInsertSQL(table, cols), 
				batchSize, values.toArray(new Collection<?>[0]));
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(String table, Map<String, ?> data) {
		Set<String> cols = data.keySet();
		List<Object> values = new LinkedList<Object>();
		for (String col : cols) {
			values.add(data.get(col));
		}
		return update(genInsertSQL(table, cols), values);
	}

	/**
	 * {@inheritDoc}
	 */
	public int update(final String sql, final Object... values) {
		return exec(new So<Integer>() {
			public Statement build() throws SQLException {
				return prepareStatement(conn, sql, Arrays.asList(values));
			}

			public Integer exec(Statement statement) throws SQLException {
				return ps(statement).executeUpdate();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMap(
			final String sql,
			final Object... values) {
		return list(new Ro<Map<String, Object>>() {
			public Map<String, Object> exec(ResultSet rs) throws SQLException {
				return readMap(rs);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMapPage(
			String sql, Collection<Order> orders, 
			int start, int limit, 
			Object... args) {
		SqlAndArgs sa = customResolver.getPaginationProcessor(conn)
				.process(orders, start, limit, sql, Arrays.asList(args));
		return query(new Ro<List<Map<String, Object>>>() {
			public List<Map<String, Object>> exec(ResultSet rs) 
					throws SQLException {
				return readMapList(rs);
			}
		}, sa.getSql(), sa.getArgs());
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
			String sql, Object... values) {
		return findOne(new Ro<Map<String, Object>>() {
			public Map<String, Object> exec(ResultSet rs) throws SQLException {
				return readMap(rs);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findOne(final Ro<T> ro, 
			final String sql, Object... values)  {
		return query(new Ro<T>() {
			public T exec(ResultSet rs) throws SQLException {
				T result = null;
				if (rs.next()) {
					result = ro.exec(rs);
				}
				if (rs.next()) {
					throw new IllegalStateException(fmt(
						"Find One By SQL [{}] Expected One Result (Or NULL) To Be Returned, " + 
						"But Found More Than One", sql));
				}
				return result;
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> list(
		final Ro<T> ro, String sql, Object... args) {
		return query(new Ro<List<T>>() {
			public List<T> exec(ResultSet rs) throws SQLException {
				List<T> result = new LinkedList<T>();
				while (rs.next()) {
					result.add(ro.exec(rs));
				}
				return result;
			}
		}, sql, args);
	}
	
	// -- 
	// private methods

	private <T> T exec(So<T> so) {
		Statement statement = null;
		try {
			return so.exec(statement = so.build());
		} 
		catch (SQLException e) {
			log.error("Execute Statement Error Caused.", e);
			throw new IllegalStateException(e);
		} 
		finally {
			if (statement != null) {
				try {
					log.debug("Close Statement [{}].", statement);
					statement.close();
				}
				catch (SQLException e) {
					log.warn("Close Statement Error Cause.", e);
				}
			}
		}
	}

	private PreparedStatement ps(Statement statement) {
		return (PreparedStatement) statement;
	}

	private Map<String, Object> readMap(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int numColumn = metaData.getColumnCount();
		Map<String, Object> mapRtn = new HashMap<String, Object>();
		for (int i = 1; i <= numColumn; ++i) {
			String colName = metaData.getColumnLabel(i);
			// ignore row number
			if (!PaginationProcessor.COLUMN_ROW_NUMBER
					.equalsIgnoreCase(colName)) {
				mapRtn.put(colName, customResolver.readValue(rs, i));
			}
		}
		return mapRtn;
	}

	private List<Map<String, Object>> readMapList(ResultSet rs) {
		List<Map<String, Object>> mapList = 
			new LinkedList<Map<String, Object>>();
		try {
			while (rs.next()) {
				mapList.add(readMap(rs));
			}
		} 
		catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return mapList;
	}

	private PreparedStatement setValues(PreparedStatement ps, 
			Collection<?> values) throws SQLException {
		if (values != null && !values.isEmpty()) {
			int i = 0;
			for (Object value : values) {
				customResolver.setParam(ps, ++i, value);
			}
		}
		return ps;
	}
	
	/**
	 * @param sql delete from users where id = :id
	 * @param mapArgs {id: 2008110101}
	 * @param outArgs [] -> [2008110101]
	 * @return delete from users where id = ?
	 */
    private String buildSql(String sql, Map<String, ?> mapArgs, List<Object> outArgs) {
        String rtnSQL;
        if (!mapArgs.isEmpty()) {
            StringBuffer newSql = new StringBuffer();
            // matches such as :user_id, :id
            Matcher matcher = Pattern.compile(":\\w+").matcher(sql); 
            while (matcher.find()) {
                List<Object> args = new LinkedList<Object>();
                // replace named arg ':user_id' to arg holder '?'
                matcher.appendReplacement(newSql,
                	genArgHolder(mapArgs.get(matcher.group().substring(1)), args));
                outArgs.addAll(args);
            }
            matcher.appendTail(newSql); 
            rtnSQL = newSql.toString();
        } 
        else {
            rtnSQL = sql;
        }
        return rtnSQL;
    }

    /**
     * @param sql delete from users where id in (?)
     * @param args
     * @param outArgs
     * @return
     */
	private String buildSql(String sql, Collection<?> args, List<Object> outArgs) {
		StringBuilder newSql = new StringBuilder();
		// avoid last ? could not be split
		String[] sqlSplitted = (sql + " ").split("\\?");
		// no ? and values is empty
		if (sqlSplitted.length == 1 && args.isEmpty()) {
			newSql.append(sql);
		}
		// expand n values for 1 '?'
		else if (sqlSplitted.length == 2 && !args.isEmpty()) {
			newSql.append(sqlSplitted[0]);
			newSql.append(genArgHolder(args, outArgs));
			newSql.append(sqlSplitted[1]);
		} 
		// size(?) == size(values)
		else if (sqlSplitted.length == args.size() + 1) {
			int i = 0;
			for (Object v : args) {
				List<Object> valuesExpanded = new LinkedList<Object>();
				newSql.append(sqlSplitted[i++]);
				newSql.append(genArgHolder(v, valuesExpanded));
				outArgs.addAll(valuesExpanded);
			}
			newSql.append(sqlSplitted[sqlSplitted.length - 1]);
		} 
		// error
		else {
			throw new IllegalStateException(
				fmt("SQL [{}] Does Not Match Args [{}]", sql, args));
		}
		return newSql.toString();
	}

	@SuppressWarnings("unchecked")
	private PreparedStatement prepareStatement(
			Connection conn, 
			String sql, 
			Collection<?> args) throws SQLException {
		String newSql;
		List<Object> newArgs = new LinkedList<Object>();
		if (args.size() == 1) {
			Object objArgs = args.iterator().next();
			if (objArgs != null && objArgs.getClass().isArray()) {
				List<Object> vh = new LinkedList<Object>();
				for (int i = 0; i < Array.getLength(objArgs); ++i) {
					vh.add(Array.get(objArgs, i));
				}
				newSql = buildSql(sql, vh, newArgs);
			} 
			else if (objArgs instanceof Collection<?>) { 
				newSql = buildSql(sql, (Collection<?>) objArgs, newArgs);
			} 
			else if (objArgs instanceof Map<?, ?>) { 
				newSql = buildSql(sql, (Map<String, ?>) objArgs, newArgs);
            } 
            else { 
				newSql = sql;
				newArgs.add(objArgs);
			}
		} 
		else if (args.size() > 1) {
			newSql = buildSql(sql, args, newArgs);
		} 
		else {
			newSql = sql;
		}
		PreparedStatement ps = conn.prepareStatement(newSql);
		// set new values
		if (!newArgs.isEmpty()) {
			setValues(ps, newArgs);
		}
		return ps;
	}	

	/**
	 * generate arg holder
	 * @param arg if v is collection, generate '?, ?...' else ?
	 * @param argsOut args out
	 * @return sql ?, ?
	 */
	private String genArgHolder(Object arg, List<Object> argsOut) {
		String sqlRtn = null;
		if (arg != null && arg.getClass().isArray()) {
			List<String> vh = new LinkedList<String>();
		    for(int i = 0; i < Array.getLength(arg); ++i){
		        argsOut.add(Array.get(arg, i));
		        vh.add("?");
		    }
			sqlRtn = StringUtils.join(vh, ", ");
		} 
		else if (arg instanceof Collection<?>) {
			Collection<?> listValues = (Collection<?>) arg;
			argsOut.addAll(listValues);
			String[] vh = new String[listValues.size()];
			Arrays.fill(vh, "?");
			sqlRtn = StringUtils.join(Arrays.asList(vh), ", ");
		} 
		else {
			argsOut.add(arg);
			sqlRtn = "?";
		}
		return sqlRtn;
	}

	private String genInsertSQL(String table, Collection<String> cols)  {
		String[] vh = new String[cols.size()];
		Arrays.fill(vh, "?");
		return new StringBuffer("insert into ")
			.append(table)
			.append(" (")
			.append(StringUtils.join(cols, ", "))
			.append(") values (")
			.append(StringUtils.join(Arrays.asList(vh), ", "))
			.append(")").toString();
	}
	
	private int sum(int[] v) {
		int result = 0;
		for (int i : v) {
			result += i;
		}
		return result;
	}

	private String fmt(String msg, Object... args) {
		return MessageFormatter.format(msg, args).getMessage();
	}
}
