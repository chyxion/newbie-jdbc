package me.chyxion.dao;

import java.util.Map;
import java.util.Set;
import java.sql.Types;
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

import me.chyxion.dao.po.SqlAndArgs;
import me.chyxion.dao.traits.AbstractDbTrait;
import me.chyxion.dao.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 20, 2015 5:28:00 PM
 */
class BasiceDAOSupport implements BasicDAO {
	private static final Logger log = 
		LoggerFactory.getLogger(BasiceDAOSupport.class);

	protected AbstractDbTrait dbTrait;
	protected Connection conn;

	public BasiceDAOSupport(Connection conn) {
		this.conn = conn;
	}

	public BasiceDAOSupport() {}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> listValue(String sql, Object... values) {
		return list(new Ro<T>() {
			@SuppressWarnings("unchecked")
			public T exec(ResultSet rs) throws SQLException {
				return (T) rs.getObject(1);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findValue(String sql, Object... values) {
		return findOne(new Ro<T>() {
			@SuppressWarnings("unchecked")
			public T exec(ResultSet rs) throws SQLException {
				return (T) rs.getObject(1);
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
	public int executeBatch(
			final String sql, 
			final int batchSize,
			final List<?>... args) {
		log.info("execute batch[{}]", sql);
		return exec(new So<Integer>() {
			public Statement build() throws SQLException {
				return conn.prepareStatement(sql);
			}

			public Integer exec(Statement statement) throws SQLException {
				int result = 0;
				int i = 0;
				for (List<?> list : args) {
					setValues(ps(statement), list);
					ps(statement).addBatch();
					// TODO test
					if (++i % batchSize == 0 && i != 0) {
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
	public int insert(String table, 
			List<String> cols, 
			Collection<List<?>> values, 
			int batchSize) {
		return executeBatch(genInsertSQL(table, cols), 
			batchSize, values.toArray(new List<?>[0]));
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
				return getMap(rs);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMapPage(
			List<Order> orders, int start, 
			int limit, String sql, 
			Object... args) {
		// conn.getMetaData();
		SqlAndArgs pair = 
				dbTrait.pageStatement(
					orders, start, limit, 
					sql, Arrays.asList(args));
		return query(new Ro<List<Map<String, Object>>>() {
			public List<Map<String, Object>> exec(ResultSet rs)
					throws SQLException {
				return getMapList(rs);
			}
		}, pair.getSql(), pair.getArgs());
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
			String sql, Object... values) {
		return findOne(new Ro<Map<String, Object>>() {
			public Map<String, Object> exec(ResultSet rs) throws SQLException {
				return getMap(rs);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findOne(final Ro<T> ro, 
			final String strSQL, Object... values)  {
		return query(new Ro<T>() {
			public T exec(ResultSet rs)  {
				try {
					T result = null;
					if (rs.next()) {
						result = ro.exec(rs);
					}
					if (rs.next()) {
						throw new IllegalStateException(
							"Find One By SQL [" + strSQL + 
							"] Expected One Result (Or NULL) To Be Returned, But Found More Than One");
					}
					return result;
				} 
				catch (SQLException e) {
					log.error("Find Value Error Caused", e);
					throw new RuntimeException(e);
				}
			}
		}, strSQL, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> list(
		final Ro<T> ro, String sql, Object... args) {
		return query(new Ro<List<T>>() {
			public List<T> exec(ResultSet rs)  {
				List<T> result = new LinkedList<T>();
				try {
					while (rs.next()) {
						result.add(ro.exec(rs));
					}
				} 
				catch (SQLException e) {
					throw new RuntimeException(e);
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
					statement.close();
				}
				catch (SQLException e) {
					log.warn("Statement Close Error Cause", e);
				}
			}
		}
	}

	private PreparedStatement ps(Statement statement) {
		return (PreparedStatement) statement;
	}

	private Map<String, Object> getMap(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int numColumn = metaData.getColumnCount();
		Map<String, Object> mapRtn = new HashMap<String, Object>();
		for (int i = 1; i <= numColumn; ++i) {
			String colName = metaData.getColumnLabel(i);
			// ignore row number
			if (colName.equalsIgnoreCase(AbstractDbTrait.COLUMN_ROW_NUMBER)) {
				continue;
			}
			Object val = null;
			int type = metaData.getColumnType(i);
			if (type == Types.CLOB) { // 将CLOB转换为String
				val = rs.getString(i);
			} 
			else if (type == Types.BLOB || 
				type == Types.BINARY || 
				type == Types.VARBINARY ||
				type == Types.LONGVARBINARY) {
					// File f = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
					// FileUtils.copyInputStreamToFile(rs.getBinaryStream(i), f);
					// TODO to bytes
					// objValue = f;
			} 
			else {
				val = rs.getObject(i);
			}
			mapRtn.put(colName, val);
		}
		return mapRtn;
	}

	private List<Map<String, Object>> getMapList(ResultSet rs) {
		List<Map<String, Object>> mapList = 
			new LinkedList<Map<String, Object>>();
		try {
			while (rs.next()) {
				mapList.add(getMap(rs));
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return mapList;
	}

	/**
	 * @param ps
	 * @param index
	 * @param value
	 */
	private void setValue(PreparedStatement ps, int index, Object value) {
		try {
			// set null
			if (value == null) { 
	            int colType = Types.VARCHAR;
	            try {
					colType = ps.getParameterMetaData().getParameterType(index);
				} 
	            catch (SQLException e) {
	            	// ignore
	            }
				ps.setNull(index, colType);
				log.info("Prepared Statement Set Value [{}]#[NULL]#[NULL]", index);
	        } 
			else {
				log.info("Prepared Statement Set Value [{}]#[{}]#[{}]", 
					index, value.getClass(), value);
				ps.setObject(index, value);
			}
		} 
		catch (Exception e) {
			log.error("Prepared Statement Set Index [{}] Value [{}] Error Caused", 
				index, value, e);
			throw new IllegalStateException(e);
		}
	}

	private PreparedStatement setValues(PreparedStatement ps, List<?> values) {
		if (values != null && !values.isEmpty()) {
			int i = 0;
			for (Object value : values) {
				setValue(ps, ++i, value);
			}
		}
		return ps;
	}

	/**
	 * @param sql select name from users where id in (:id)
	 * @param mapArgs {"id": ["2008110101", "2008110102"]}
	 * @param outArgs ["2008110101", "2008110102"]
	 * @return select name from users where id in (?, ?)
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
	 * @param sql select name from users where gender = ? and id in (?)
	 * @param args ["F", ["2008110101", "2008110102"]]
	 * @param outArgs ["F", "2008110101", "2008110102"]
	 * @return select name from users where gender = ? and id in (?, ?)
	 */
	private String buildSql(String sql, Collection<?> args, List<Object> outArgs) {
		StringBuilder newSql = new StringBuilder();
		// avoid last ? could not be split
		String[] sqlSplitted = (sql + " ").split("\\?");
		// no ? and values is empty
		if (sqlSplitted.length == 1 && args.isEmpty()) {
			newSql.append(sql);
		}
		// expand values for one ?
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

		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	private PreparedStatement prepareStatement(
			Connection conn, 
			String sql, 
			Collection<?> args) throws SQLException {
		String newSql;
		List<Object> newArgs = new LinkedList<Object>();
		if (args.size() == 1) {
			Object oValues = args.iterator().next();
			if (oValues != null && oValues.getClass().isArray()) {
				List<Object> vh = new LinkedList<Object>();
				for (int i = 0; i < Array.getLength(oValues); ++i) {
					vh.add(Array.get(oValues, i));
				}
				newSql = buildSql(sql, vh, newArgs);
			} 
			else if (oValues instanceof List<?>) { 
				newSql = buildSql(sql, (List<?>) oValues, newArgs);
			} 
			else if (oValues instanceof Map<?, ?>) { 
				newSql = buildSql(sql, (Map<String, ?>) oValues, newArgs);
            } 
            else { 
				newSql = sql;
				newArgs.add(oValues);
			}
		} 
		if (args.size() > 1) {
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
	 * @param argsOut
	 * @return
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
