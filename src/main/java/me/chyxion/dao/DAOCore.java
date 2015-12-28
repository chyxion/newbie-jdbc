package me.chyxion.dao;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import me.chyxion.dao.traits.AbstractDbTrait;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 20, 2015 5:28:00 PM
 */
class DAOCore implements BasicDAO {
	private static final Logger log = 
		LoggerFactory.getLogger(DAOCore.class);

	protected AbstractDbTrait dbTrait;
	protected Connection conn;

	public DAOCore(Connection conn) {
		this.conn = conn;
	}

	public DAOCore() {}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> listValue(String strSQL, Object... values) {
		return query(new Ro<List<T>>() {
			@SuppressWarnings("unchecked")
			public List<T> run(ResultSet rs)  {
				List<T> result = new LinkedList<T>();
				try {
					while (rs.next()) {
						result.add((T) rs.getObject(1));
					}
				} 
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
				return result;
			}
		}, strSQL, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T findValue(String sql, Object... values) {
		return findOne(new ResultSetReader<T>() {
			@SuppressWarnings("unchecked")
			public T read(ResultSet rs) throws SQLException {
				return (T) rs.getObject(1);
			}
		}, sql, values);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T query(Ro<T> rso, String strSQL, Object ... values) {
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = prepareStatement(
				conn, strSQL, Arrays.asList(values));
			rs = statement.executeQuery();
			return rso.run(rs);
		} 
		catch (Throwable e) {
			log.error("Query Error Caused", e);
			throw new IllegalStateException(e);
		} 
		finally {
			close(statement, rs);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean execute(String sql) {
		PreparedStatement statement = null;
		try {
			log.info("Execute SQL [{}]", sql);
			statement = conn.prepareStatement(sql);
			return statement.execute();
		} 
		catch (SQLException e) {
			log.error("Execute SQL [{}] Error Caused", sql);
			throw new IllegalStateException(e);
		} 
		finally {
			close(statement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(Collection<String> sqls, int batchSize) {
		Statement statement = null;
		try {
			statement = conn.createStatement();
			int i = 0;
			int result = 0;
			for (String sql : sqls) {
				statement.addBatch(sql);
				if (++i > batchSize) {
					i = 0;
					result += sum(statement.executeBatch());
				}
			}
			result += sum(statement.executeBatch());
			return result;
		}
		catch (SQLException e) {
			log.error("Execute Batch SQLs [{}] Error Caused", sqls);
			throw new RuntimeException(e);
		}
		finally {
			close(statement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeBatch(String sql, 
			Collection<List<?>> jaValues,
			int batchSize) {
		PreparedStatement statement = null;
		try {
			log.info("execute batch[{}]", sql);
			statement = conn.prepareStatement(sql);
			int result = 0;
			int i = 0;
			for (List<?> list : jaValues) {
				setValues(statement, list);
				statement.addBatch();
				// TODO test
				if (++i % batchSize == 0 && i != 0) {
					result += sum(statement.executeBatch());
				}
			}
			result += sum(statement.executeBatch());
			return result;
		}
		catch (Exception e) {
			log.error("Excute Batch [{}] Error Cause", sql, e);
			throw new RuntimeException(e);
		}
		finally {
			close(statement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int insert(String table, 
			List<String> cols, 
			Collection<List<?>> jaValues, 
			int batchSize) {
		return executeBatch(genInsertSQL(table, cols), jaValues, batchSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public int update(final String sql, final Object... values) {
		PreparedStatement statement = null;
		try {
			statement = prepareStatement(
				conn, sql, Arrays.asList(values));
			return statement.executeUpdate();
		}
		catch (SQLException e) {
			log.error("Update SQL [{}] Values [{}] Error Caused", sql, values);
			throw new RuntimeException(e);
		}
		finally {
			close(statement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMap(
			final String sql,
			final Object... values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = prepareStatement(conn, 
				sql, Arrays.asList(values));
			resultSet = statement.executeQuery();
			return getMapList(resultSet);
		}
		catch (SQLException e) {
			log.error("List Map SQL [{}] Values [{}] Error Caused", sql, values);
			throw new RuntimeException(e);
		}
		finally {
			close(statement, resultSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> listMapPage(
			List<Order> orders, int start, 
			int limit, String sql, 
			Object... values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			Pair<String, Collection<?>> pair = 
				dbTrait.pageStatement(
					orders, start, limit, 
					sql, Arrays.asList(values));
			statement = prepareStatement(
				conn, pair.getKey(), pair.getValue());
			return getMapList(statement.executeQuery());
		}
		catch (SQLException e) {
			log.error("Query Map Page SQL [{}] Values [{}] Error Caused", sql, values);
			throw new RuntimeException(e);
		}
		finally {
			close(statement, resultSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> findMap(
			String sql, Object... values) {
		return findOne(new ResultSetReader<Map<String, Object>>() {
			public Map<String, Object> read(ResultSet rs) {
				return getMap(rs);
			}
		}, sql, values);
	}

	public Map<String, Object> getMap(ResultSet rs) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numColumn = metaData.getColumnCount();
			Map<String, Object> mapModel = new HashMap<String, Object>();
			for (int i = 1; i <= numColumn; ++i) {
				String colName = metaData.getColumnLabel(i);
				// ignore row number
				if (colName.equalsIgnoreCase(AbstractDbTrait.COLUMN_ROW_NUMBER)) {
					continue;
				}
				Object objValue = null;
				int type = metaData.getColumnType(i);
				if (type == Types.CLOB) { // 将CLOB转换为String
					objValue = rs.getString(i);
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
					objValue = rs.getObject(i);
				}
				mapModel.put(colName, objValue);
			}
			return mapModel;
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * get map list from result set
	 * @param resultSet
	 * @return
	 */
	public List<Map<String, Object>> getMapList(ResultSet resultSet) {
		List<Map<String, Object>> mapList = new LinkedList<Map<String, Object>>();
		try {
			while (resultSet.next()) {
				mapList.add(getMap(resultSet));
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return mapList;
	}

	/**
	 * 关闭connection, statement, resultSet
	 * @param dbConnection
	 * @param statement
	 * @param resultSet
	 */
	public void close(Connection dbConnection, 
			Statement statement,
			ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void close(Connection dbConnection) {
		close(dbConnection, null, null);
	}

	public void close(Statement statement, ResultSet rs) {
		close(null, statement, rs);
	}

	public void close(Statement statement) {
		close(null, statement, null);
	}

	public void close(ResultSet rs) {
		close(null, null, rs);
	}

	/**
	 * @param ps
	 * @param index
	 * @param value
	 */
	public void setValue(PreparedStatement ps, int index, Object value) {
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
				log.info("Prepared Statement Set Value [{}]#[{}]#[{}]", index, value.getClass(), value);
				ps.setObject(index, value);
			}
		} 
		catch (Exception e) {
			log.error("Prepared Statement Set Index [{}] Value [{}] Error Caused", index, value, e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param ps
	 * @param values
	 * @return
	 */
	public PreparedStatement setValues(PreparedStatement ps, List<?> values) {
		if (values != null && !values.isEmpty()) {
			int i = 0;
			for (Object value : values) {
				setValue(ps, ++i, value);
			}
		}
		return ps;
	}

	/**
	 * JSONObject传入值生成PreparedStatement
	 * @param dbConnection 数据库连接
	 * @param sql： select name from users where id in (:id)
	 * @param mapValues {"id": ["2008110101", "2008110102"]}
	 * @param outValues ["2008110101", "2008110102"]
	 * @return select name from users where id in (?, ?)
	 */
    public String buildSQL(String sql, Map<String, ?> mapValues, List<Object> outValues) {
        String rtnSQL;
        if (!mapValues.isEmpty()) {
            StringBuffer newSql = new StringBuffer();
            // matches such as :user_id, :id
            Matcher matcher = Pattern.compile(":\\w+").matcher(sql); 
            while (matcher.find()) {
                List<Object> args = new LinkedList<Object>();
                // replace named arg ':user_id' to arg holder '?'
                matcher.appendReplacement(newSql,
                	genArgHolder(mapValues.get(matcher.group().substring(1)), args));
                outValues.addAll(args);
            }
            matcher.appendTail(newSql); // 将最后未匹配部分追加到sbSQL
            rtnSQL = newSql.toString();
        } 
        else {
            rtnSQL = sql;
        }
        return rtnSQL;
    }

	/**
	 * List<Object> 传入值构建PreparedStatement
	 * @param dbConnection 数据库连接
	 * @param sql 如： select name from users where gender = ? and id in (?)
	 * @param rawValues ["F", ["2008110101", "2008110102"]]
	 * @param outValues ["F", "2008110101", "2008110102"]
	 * @return select name from users where gender = ? and id in (?, ?)
	 * @
	 */
	public String buildSql(String sql, Collection<?> rawValues, List<Object> outValues) {
		StringBuilder newSql = new StringBuilder();
		// avoid last ? could not be split
		String[] sqlSplitted = (sql + " ").split("\\?");
		// no ? and values is empty
		if (sqlSplitted.length == 1 && rawValues.isEmpty()) {
			newSql.append(sql);
		}
		// expand values for one ?
		else if (sqlSplitted.length == 2 && !rawValues.isEmpty()) {
			newSql.append(sqlSplitted[0]);
			newSql.append(genArgHolder(rawValues, outValues));
			newSql.append(sqlSplitted[1]);
		} 
		// size(?) == size(values)
		else if (sqlSplitted.length == rawValues.size() + 1) {
			int i = 0;
			for (Object v : rawValues) {
				List<Object> valuesExpanded = new LinkedList<Object>();
				newSql.append(sqlSplitted[i++]);
				newSql.append(genArgHolder(v, valuesExpanded));
				outValues.addAll(valuesExpanded);
			}
			newSql.append(sqlSplitted[sqlSplitted.length - 1]);
		} 
		// error
		else {
			throw new IllegalStateException(
				MessageFormatter.format("SQL [{}] Does Not Match Args [{}]", sql, rawValues)
					.getMessage());
		}

		return sql.toString();
	}

	/**
	 * @param conn
	 * @param sql
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PreparedStatement prepareStatement(
			Connection conn, 
			String sql, 
			Collection<?> values) {
		String newSql;
		List<Object> newValues = new LinkedList<Object>(); // 展开集合后新的值
		PreparedStatement ps;
		if (values.size() == 1) {
			Object oValues = values.iterator().next();
			if (oValues != null && oValues.getClass().isArray()) {
				List<Object> vh = new LinkedList<Object>();
				for (int i = 0; i < Array.getLength(oValues); ++i) {
					vh.add(Array.get(oValues, i));
				}
				newSql = buildSql(sql, vh, newValues);
			} 
			else if (oValues instanceof List<?>) { 
				newSql = buildSql(sql, (List<?>) oValues, newValues);
			} 
			else if (oValues instanceof Map<?, ?>) { 
				newSql = buildSQL(sql, (Map<String, ?>) oValues, newValues);
            } 
            else { 
				newSql = sql;
				newValues.add(oValues);
			}
		} 
		if (values.size() > 1) {
			newSql = buildSql(sql, values, newValues);
		} 
		else {
			newSql = sql;
		}

		try {
			ps = conn.prepareStatement(newSql);
		} 
		catch (SQLException e) {
			log.error("Prepare Statement [{}] Error Caused", newSql);
			throw new IllegalStateException(e);
		}

		// set new values
		if (!newValues.isEmpty()) {
			setValues(ps, newValues);
		}
		return ps;
	}	

	/**
	 * 展开传入值集合对象，生成值占位如：?, ?, ?
	 * @param v 传入值，如果是集合，则生成如：?, ?, ?, ?，否则生成：?
	 * @param valuesExpanded 返回展开值
	 * @return 生成预备SQL
	 */
	public String genArgHolder(Object v, List<Object> valuesExpanded) {
		String sqlRtn = null;
		if (v != null && v.getClass().isArray()) {
			List<String> vh = new LinkedList<String>();
		    for(int i = 0; i < Array.getLength(v); ++i){
		        valuesExpanded.add(Array.get(v, i));
		        vh.add("?");
		    }
			sqlRtn = StringUtils.join(vh, ", ");
		} 
		else if (v instanceof Collection<?>) {
			Collection<?> listValues = (Collection<?>) v;
			valuesExpanded.addAll(listValues);
			String[] vh = new String[listValues.size()];
			Arrays.fill(vh, "?");
			sqlRtn = StringUtils.join(vh, ", ");
		} 
		else {
			valuesExpanded.add(v);
			sqlRtn = "?";
		}
		return sqlRtn;
	}

	public String genInsertSQL(String table, List<String> cols)  {
		String[] vh = new String[cols.size()];
		Arrays.fill(vh, "?");
		return new StringBuffer("inser into ")
			.append(table)
			.append(" (")
			.append(StringUtils.join(cols, ", "))
			.append(") values (")
			.append(StringUtils.join(vh, ", "))
			.append(")").toString();
	}
	
	private int sum(int[] v) {
		int result = 0;
		for (int i : v) {
			result += i;
		}
		return result;
	}
	
	private <T> T findOne(final ResultSetReader<T> reader, 
			final String strSQL, Object... values)  {
		return query(new Ro<T>() {
			public T run(ResultSet rs)  {
				try {
					T result = null;
					if (rs.next()) {
						result = reader.read(rs);
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
	
	private static interface ResultSetReader<T> {
		T read(ResultSet rs) throws SQLException;
	}
}
