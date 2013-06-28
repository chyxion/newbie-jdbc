package com.shs.framework.dao;
import java.io.File;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.shs.framework.dao.traits.IDbTrait;
import com.shs.framework.dao.traits.StatementWrapper;
import com.shs.framework.utils.JSONUtils;

/**
 * Describe: 基础数据库访问类
 * @version 0.1
 * Date Created: Mar 28, 2012 2:30:19 PM
 * @author chyxion
 * Support: chyxion@163.com
 * Date Modified:
 * Modified by:
 * Copyright:
 */
@SuppressWarnings("unchecked")
public final class BaseDAO {
	/**
	 * 数据库特征
	 */
	private IDbTrait dbTrait = DbManager.getDbTrait();
	public void setDbTrait(IDbTrait dbt) {
		dbTrait = dbt;
	}
	private static Logger logger = Logger.getLogger(BaseDAO.class);
	/**
	 * SQL batch size
	 */
	public static int batchSize = 1024;
	/**
	 * 默认字符小写
	 */
	private boolean lowerCase = DbManager.LOWERCASE;
	/**
	 * 启用事件
	 */
	private IEventHandler eventHandler;
	public IEventHandler getEventHandler() {
		return eventHandler;
	}
	public BaseDAO setEventHandler(IEventHandler e) {
		eventHandler = e;
		return this;
	}
	public BaseDAO setLowerCase(boolean lowerCase) {
		this.lowerCase = lowerCase;
		return this;
	}
	/**
	 * 获得连接
	 */
	public Connection getConnection()  {
		return DbManager.getConnection();
	}

    /**
     * 查找一个字符串，如： 
     * findStr("select name from users where id = 110101") => "chyxion"
     * @param strSQL 查询SQL字符串
     * @param values <Object[]>, List<Object>, <JSONArray>
     * @return 返回查找到的值或者空
     * @
     */
	public String findStr(final String strSQL, final Object ... values)  {
		return findObj(strSQL, values);
	}
	public <T> T findObj(final String strSQL, final Object ... values)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = findObj(strSQL, values);
			}
		});
	}
	/**
	 * 查找一个String
	 * @param dbConnection
	 * @param strSQL 
     * @param values <Object[]>, List<Object>, <JSONArray>
	 * @return
	 * @
	 */
	public String findStr(Connection dbConnection, String strSQL, Object ... values) {
		return findObj(dbConnection, strSQL, values);
	}
	public <T> T findObj(Connection dbConnection, String strSQL, Object ... values)  {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).findObj(strSQL, values);
	}
	/**
	 * 查询返回List<String>
	 * @param dbConnection
	 * @param strSQL
	 * @param values，可选
	 * @return
	 * @
	 */
	public List<String> findStrList(Connection dbConnection, String strSQL, Object ... values) {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).findStrList(strSQL, values);
	}
	/**
	 * 查询返回List<String>
	 * @param strSQL
	 * @param values，可选
	 * @return
	 * @
	 */
	public List<String> findStrList(final String strSQL, final Object ... values) {
		return execute(new ConnectionOperator() {
			@Override
			public void run() {
				result = findStrList(strSQL, values);
			}
		});
	}
	public int findInt(final String strSQL, final Object ... values) {
		return findObj(strSQL, values);
	}
	public int findInt(Connection dbConnection, String strSQL, Object ... values) {
		return findObj(dbConnection, strSQL, values);
	}
	public int findLong(final String strSQL, final Object ... values) {
		return findObj(strSQL, values);
	}
	public int findLong(Connection dbConnection, String strSQL, Object ... values) {
		return findObj(dbConnection, strSQL, values);
	}
	public int findDouble(final String strSQL, final Object ... values) {
		return findObj(strSQL, values);
	}
	public int findDouble(Connection dbConnection, String strSQL, Object ... values) {
		return findObj(dbConnection, strSQL, values);
	}
	/**
	 * @param strSQL
	 * @param values
	 * @param rso
	 * @return
	 */
	public <T> T query(final ResultSetOperator rso, final String strSQL, final Object ... values)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = new DAOCore(lowerCase, dbConnection, dbTrait, event).query(rso, strSQL, values);
			}
		});
	}
	/**
	 * @param dbConnection
	 * @param strSQL
	 * @param values
	 * @param rso
	 * @return
	 */
	public <T> T query(Connection dbConnection, ResultSetOperator rso, String strSQL, Object ... values)  {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).query(rso, strSQL, values);
	}
	/**
	 * 执行Connection的操作, 参数为Connection操作器,
	 * 注意，该操作不带事物，只是使用同一个连接，如需执行事务，请使用executeTransaction
	 * @param co
	 * @
	 */
	public <T> T execute(ConnectionOperator co)  {
		Connection dbConnection = null;
		try {
			dbConnection = getConnection();
			co.lowerCase = lowerCase;
			co.event = eventHandler;
            co.dbTrait = dbTrait;
            co.dbConnection = dbConnection;
			co.run();
			return (T) co.result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			close(co.preparedStatement);
			close(dbConnection, co.statement, co.resultSet);
		}
	}
	public boolean execute(Connection dbConnection, String strSQL)  {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).execute(strSQL);
	}
	public boolean execute(final String strSQL)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = execute(strSQL);
			}
		});
	}
	/**
	 * 执行事务
	 * @param co
	 * @return
	 * @
	 */
	public <T> T executeTransaction(ConnectionOperator co) {
		Connection dbConnection = null;
		try {
			dbConnection = getConnection();
			dbConnection.setAutoCommit(false);
            co.dbConnection = dbConnection;
			co.lowerCase = lowerCase;
			co.event = eventHandler;
            co.dbTrait = dbTrait;
			co.run();
			dbConnection.commit();
			return (T) co.result;
		} catch (Exception e) {
			if (dbConnection != null)
				try {
					dbConnection.rollback();
				} catch (SQLException se) {
					throw new RuntimeException(se);
				}
			throw new RuntimeException(e);
		} finally {
			close(co.preparedStatement);
			close(dbConnection, co.statement, co.resultSet);
		}
	}
	/**
	 * @class describe: ResultSet操作器, 抽象类，供改写，
	 * 	操作ResultSet，无需操作后关闭处理，如需返回值，将返回值结果赋予result
	 * @version 0.1
	 * @date created: Apr 17, 2012 4:57:22 PM
	 * @author chyxion
	 * @support: chyxion@163.com
	 * @date modified: 
	 * @modified by: 
	 * @copyright: 
	 */
	public static abstract class ResultSetOperator {
		protected Object result;
        protected ResultSet resultSet;
        protected Connection dbConnection;
		protected abstract void run() throws Exception ;
	}
	/**
	 * @class describe: Connection操作器，抽象类，自动关连接，
	 * 	如需返回值，请将返回对象赋予result
	 * @version 0.1
	 * @date created: Apr 17, 2012 4:58:16 PM
	 * @author chyxion
	 * @support: chyxion@163.com
	 * @date modified: 
	 * @modified by: 
	 * @copyright: 
	 */
	public static abstract class ConnectionOperator extends DAOCore {
		protected Object result;
		protected Statement statement;
		protected PreparedStatement preparedStatement;
		protected ResultSet resultSet;
		protected abstract void run() throws Exception;
	}

	/**
	 * 批量执行
	 * @param listSQLs
	 * @return 更新的数据行数 数组。
	 */
	public void executeBatch(final List<String> listSQLs)  {
		executeTransaction(new ConnectionOperator() {
			@Override
			public void run()  {
				executeBatch(listSQLs);
			}
		});
	}
	/**
	 * @param dbConnection
	 * @param listSQLs
	 * @
	 */
	public void executeBatch(Connection dbConnection, List<String> listSQLs)
			 {
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			int i = 0;
			for (String sql : listSQLs) {
				statement.addBatch(sql);
				if (++i > batchSize) {
					i = 0;
					statement.executeBatch();
				}
			}
			statement.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(statement);
		}
	}
	/**
	 * 执行批语句
	 * @param strSQL, insert into foobar (?, ?)
	 * @param values, [[0, 1], [2, 3]]
	 * @
	 */
	public void executeBatch(final String strSQL, final List<Object> values) {
		executeBatch(strSQL, new JSONArray(values));
	}
	public void executeBatch(Connection dbConnection, final String strSQL, final List<Object> values)  {
		executeBatch(dbConnection, strSQL, new JSONArray(values));
	}
	/**
	 * 执行批SQL
	 * @param strSQL, insert into foobar (?, ?)
	 * @param jaValues, [[0, 1], [2, 3]]
	 * @
	 */
	public void executeBatch(final String strSQL, final JSONArray jaValues) {
		executeTransaction(new ConnectionOperator() {
			@Override
			public void run()  {
				executeBatch(strSQL, jaValues);
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
	public void executeBatch(Connection dbConnection, String strSQL, JSONArray jaValues)  {
		PreparedStatement preparedStatement = null;
		try {
			new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).executeBatch(strSQL, jaValues);
		} finally {
			close(preparedStatement);
		}
	}
	public void insert(Connection dbConnection, String table, JSONArray jaFields, JSONArray jaValues)  {
		new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).insert(table, jaFields, jaValues);
	}
	/**
	 * 从resultSet中返回一个JSONObject, JSON字段
	 * @param resultSet
	 * @return
	 * @
	 */
	public JSONObject getJSONObject(ResultSet resultSet) {
		return getJSONObject(resultSet, lowerCase);
	}
	/**
	 * @param rs
	 * @param lc 指示JSONObject字段名称的小写
	 * @return
	 * @
	 */
	public JSONObject getJSONObject(ResultSet rs, boolean lc) {
		return new JSONObject(getMap(rs, lc));
	}
	public static Map<String, Object> getMap(ResultSet rs, boolean lc) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numColumn = metaData.getColumnCount();
			Map<String, Object> mapModel = new HashMap<String, Object>();
			for (int i = 1; i <= numColumn; ++i) {
				String colName = metaData.getColumnLabel(i);// 得到列名称, 如果需要小写，则转换为小写，否则不变，另外，如果其中包含小写，则不转换
				// 跳过行编号
				if (colName.equalsIgnoreCase(IDbTrait.COLUMN_ROW_NUMBER)) {
					continue;
				}
				// 列名中混合大小写，不转换
				if (Pattern.compile("[a-z]").matcher(colName).find() && 
						Pattern.compile("[A-Z]").matcher(colName).find()) {
					// 什么都不做
				} else {
					colName = lc ? 
						colName.toLowerCase() : colName.toUpperCase();
				}
				Object objValue;
				int type = metaData.getColumnType(i);
				if (type == Types.CLOB) { // 将CLOB转换为String
					objValue = rs.getString(i);
				// 转存为临时文件
				} else if (type == Types.BLOB || 
						type == Types.BINARY || 
						type == Types.VARBINARY ||
						type == Types.LONGVARBINARY) {
					File f = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
					FileUtils.copyInputStreamToFile(rs.getBinaryStream(i), f);
					objValue = f;
				} else {
					objValue = rs.getObject(i);
				}
				mapModel.put(colName, objValue);
			}
			return mapModel;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从resultSet中返回JSONArray, JSON属性名称和select字段名称或者其别名相同, 字段名称大写
	 * @param resultSet
	 * @return
	 */
	public JSONArray getJSONArray(ResultSet resultSet) {
		return getJSONArray(resultSet, lowerCase);
	}
	public List<Map<String, Object>> 
		getMapList(ResultSet resultSet) {
		return getMapList(resultSet, lowerCase);
	}
	/**
	 * 从resultSet中返回JSONArray, 
	 * @param resultSet
	 * @param lc 指示JSON属性字段名称小写
	 * @return JSONArray
	 * @
	 */
	public JSONArray getJSONArray(ResultSet resultSet, boolean lc)
			 {
		return new JSONArray(getMapList(resultSet, lc));
	}
	/**
	 * 从resultSet中返回List<Map<String, Object>>
	 * @param resultSet
	 * @param lc
	 * @return List<Map<String, Object>>
	 * @
	 */
	public static List<Map<String, Object>> 
		getMapList(ResultSet resultSet, boolean lc) {
		List<Map<String, Object>> mapList = new LinkedList<Map<String, Object>>();
		try {
			while (resultSet.next()) {
				mapList.add(getMap(resultSet, lc));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return mapList;
	}
	/**
	 * 返回表数据行数.
	 * @param table
	 * @return 
	 * @ 
	 */
	public int count(final String table)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = count(table);
			}
		});
	}
	/**
	 * 返回表数据行数
	 * @param dbConnection
	 * @param table
	 * @return
	 * @
	 */
	public int count(Connection dbConnection, String table)  {
		return findInt("select count(1) from " + table);
	}
	/**
	 * 关闭connection, statement, resultSet
	 * @param dbConnection
	 * @param statement
	 * @param resultSet
	 */
	public static void close(Connection dbConnection, Statement statement,
			ResultSet resultSet) {
		if (resultSet != null)
			try {
				resultSet.close();
				resultSet = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (statement != null)
			try {
				statement.close();
				statement = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (dbConnection != null)
			try {
				dbConnection.close();
				dbConnection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	public static void close(Connection dbConnection) {
		close(dbConnection, null, null);
	}
	public static void close(Statement statement, ResultSet rs) {
		close(null, statement, rs);
	}
	public static void close(Statement statement) {
		close(null, statement, null);
	}
	public static void close(ResultSet rs) {
		close(null, null, rs);
	}
	/**
	 * 设置prepareStatement的index指示的值
	 * @param ps
	 * @param index
	 * @param value
	 */
	public static void setValue(PreparedStatement ps, int index, Object value) {
		try {
			if (value == null || value.equals(JSONObject.NULL)) { // 设置null
	            ParameterMetaData pmd = ps.getParameterMetaData();
	            /**
	             * 大部分数据库识别VARCHAR，Oracle不识别NULL
	             */
	            int colType = Types.VARCHAR;
	            // Oracle不支持此方法
	            try {
					colType = pmd.getParameterType(index);
				} catch (SQLException e) {}
				ps.setNull(index, colType);
				logger.debug("Prepared Statement Set Value: [" + index + "][NULL][" + value + "]");
	        } else if (value instanceof JSONObject || value instanceof JSONArray) {
				logger.debug("Prepared Statement set Value: [" + index + "][" + value.getClass()+ "][" + value + "]");
				// JSON数据，
	            ps.setString(index, value.toString());
			} else if (value instanceof File) {
				ps.setBinaryStream(index, FileUtils.openInputStream((File) value));
			} else {
				logger.debug("Prepared Statement Set Value: [" + index + "][" + value.getClass()+ "][" + value + "]");
				ps.setObject(index, value);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 为preparedStatement设值（集合）,
	 * 值集合类型可以为：Object[], JSONArray, List<Object>
	 * @param ps
	 * @param values
	 * @
	 */
	public static PreparedStatement setValues(PreparedStatement ps, Object values) {
		if (values != null) {
			if (values instanceof Object[]) {
				Object[] objArrayValues = (Object[]) values;
				for (int i = 0; i < objArrayValues.length; ++i) 
					setValue(ps, i + 1, objArrayValues[i]);
			} else if (values instanceof JSONArray) {
				JSONArray jaValues = (JSONArray) values;
				try {
					for (int i = 0; i < jaValues.length(); ++i)
						setValue(ps, i + 1, jaValues.get(i));
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			} else if (values instanceof List<?>) {
				List<Object> listValues = (List<Object>) values;
				int i = 0;
				for (Object value : listValues) 
					setValue(ps, ++i, value);
			} else { // 尝试作为一个值赋给preparedStatement
				setValue(ps, 1, values);
			}
		}
		return ps;
	}
	/**
	 * JSONArray 构建PreparedStatement 
	 * @param dbConnection 数据库连接
	 * @param strSQL  如： select age from users where id in (?) and name = ?
	 * @param jaValues 存放设置值得JSONArray, 如： [[2008110101, 2008110102], "chyxion"]
	 * @param outValues 生成新的值集合，如： [2008110101, 2008110102, "chyxion"]
	 * @return select age from users where id in (?, ?) and name = ?
	 * @
	 */
	public static String buildSQL(String strSQL, JSONArray jaValues, List<Object> outValues) {
        String rtnSQL;
        StringBuffer sbSQL = new StringBuffer(); // 重新构造SQL
		String[] saSQL = (strSQL + " ").split("\\?"); // 加上最后空格，否则如果最后一个为?占位，则拆分将会少一个元素
		if (saSQL.length == 2 && jaValues.length() > 1){ // 如果?数量少于jaValues数量，展开
			sbSQL.append(saSQL[0]);
			sbSQL.append(genValueHolder(jaValues, outValues));
			rtnSQL = sbSQL.append(saSQL[1]).toString();
		} else if (jaValues.length() > 0) { // 分析传入值，构建SQL
			try {
				for (int i = 0; i < jaValues.length(); i++) {
					List<Object> valuesExpanded = new LinkedList<Object>();
					sbSQL.append(saSQL[i]);
					sbSQL.append(genValueHolder(jaValues.get(i), valuesExpanded));
					outValues.addAll(valuesExpanded);
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			rtnSQL = sbSQL.append(saSQL[saSQL.length - 1]).toString();
		} else {
            rtnSQL = strSQL;
        }
		return rtnSQL;
	}
	/**
	 * Object Array 为传入值构建PreparedStatement
	 * @param dbConnection 数据库连接
	 * @param strSQL : select gender from users where name = ?
	 * @param oaValues ["chyxion"]
	 * @param outValues ["chyxion"]
	 * @return select gender from users where name = ?
	 * @
	 */
	public static String buildSQL(String strSQL, Object[] oaValues, List<Object> outValues) {
        String rtnSQL;
        StringBuffer sbSQL = new StringBuffer(); // 重新构造SQL
		String[] saSQL = (strSQL + " ").split("\\?"); // 加上最后空格，否则如果最后一个为?占位，则拆分将会少一个元素
		if (saSQL.length == 2 && oaValues.length > 1) { // 如果占位符?数量少于传入值oaVaulues，展开
			sbSQL.append(saSQL[0]);
			sbSQL.append(genValueHolder(oaValues, outValues));
	        rtnSQL = sbSQL.append(saSQL[1]).toString();
		} else if (oaValues.length > 0) {
			for (int i = 0; i < oaValues.length; i++) { // 遍历传入值，构建SQL
				List<Object> valuesExpanded = new LinkedList<Object>();
				sbSQL.append(saSQL[i]);
				sbSQL.append(genValueHolder(oaValues[i], valuesExpanded));
				outValues.addAll(valuesExpanded);
			}
			rtnSQL = sbSQL.append(saSQL[saSQL.length - 1]).toString();
		} else {
            rtnSQL = strSQL;
        }
		return rtnSQL;
	}
	/**
	 * JSONObject传入值生成PreparedStatement
	 * @param dbConnection 数据库连接
	 * @param strSQL： select name from users where id in (:id)
	 * @param joValues {"id": ["2008110101", "2008110102"]}
	 * @param outValues ["2008110101", "2008110102"]
	 * @return select name from users where id in (?, ?)
	 * @
	 */
	public static String buildSQL(String strSQL, JSONObject joValues, List<Object> outValues) {
        String rtnSQL;
        if (joValues.length() > 0) {
            StringBuffer sbSQL = new StringBuffer(); // 重新构造SQL
            Matcher matcher = Pattern.compile(":\\w+").matcher(strSQL); // 匹配strSQL中的形如:user_id, :id
            try {
	            while(matcher.find()) {
	                List<Object> valuesExpanded = new LinkedList<Object>(); // 存放展开值
	                matcher.appendReplacement(sbSQL,
	                        genValueHolder(joValues.get(matcher.group().substring(1)), // 取得JSONObject中匹配的值, :id => id
	                        valuesExpanded));  // 替换匹配值的占位
	                outValues.addAll(valuesExpanded); // 将展开值加入到新值集合
	            }
            } catch (JSONException e) {
            	throw new RuntimeException(e);
			}
            matcher.appendTail(sbSQL); // 将最后未匹配部分追加到sbSQL
            rtnSQL = sbSQL.toString();
        } else {
            rtnSQL = strSQL;
        }
		return rtnSQL;
	}
    public static String buildSQL(String strSQL, Map<String, Object> mapValues, List<Object> outValues) {
        String rtnSQL;
        if (mapValues.size() > 0) {
            StringBuffer sbSQL = new StringBuffer(); // 重新构造SQL
            Matcher matcher = Pattern.compile(":\\w+").matcher(strSQL); // 匹配strSQL中的形如:user_id, :id
            while(matcher.find()) {
                List<Object> valuesExpanded = new LinkedList<Object>(); // 存放展开值
                matcher.appendReplacement(sbSQL,
                        genValueHolder(mapValues.get(matcher.group().substring(1)), // 取得JSONObject中匹配的值, :id => id
                                valuesExpanded));  // 替换匹配值的占位
                outValues.addAll(valuesExpanded); // 将展开值加入到新值集合
            }
            matcher.appendTail(sbSQL); // 将最后未匹配部分追加到sbSQL
            rtnSQL = sbSQL.toString();
        } else {
            rtnSQL = strSQL;
        }
        return rtnSQL;
    }
	/**
	 * List<Object> 传入值构建PreparedStatement
	 * @param dbConnection 数据库连接
	 * @param strSQL 如： select name from users where gender = ? and id in (?)
	 * @param listValues ["F", ["2008110101", "2008110102"]]
	 * @param outValues ["F", "2008110101", "2008110102"]
	 * @return select name from users where gender = ? and id in (?, ?)
	 * @
	 */
	public static String buildSQL(String strSQL, List<Object> listValues, List<Object> outValues) {
		StringBuffer sbSQL = new StringBuffer(); // 重新构造SQL
		String[] saSQL = (strSQL + " ").split("\\?"); // 加上最后空格，否则如果最后一个为?占位，则拆分将会少一个元素
		if (saSQL.length == 2 && listValues.size() > 1) { // 如果占位?数量少于传入值listValues，展开占位
			sbSQL.append(saSQL[0]);
			sbSQL.append(genValueHolder(listValues, outValues));
			sbSQL.append(saSQL[1]);
		} else if (listValues.size() > 0) { // 分析传入值，生成SQL
			int i = 0;
			for (Object v : listValues) {
				List<Object> valuesExpanded = new LinkedList<Object>();
				sbSQL.append(saSQL[i++]);
				sbSQL.append(genValueHolder(v, valuesExpanded));
				outValues.addAll(valuesExpanded);
			}
			sbSQL.append(saSQL[saSQL.length - 1]);
		} else { // list size equals 0
           return strSQL;
        }
		return sbSQL.toString();
	}
	/**
	 * 生成preparedStatement
	 * @param dbConnection
	 * @param strSQL
	 * @param values, 可以是Object[], List<Object>, JSONArray, Object（单个值）
	 * @return
	 * @
	 */
	public static StatementWrapper prepareStatement(Connection dbConnection, 
			String strSQL, 
			Object ... values)  {
		String newSQL; // 重新构造SQL
		List<Object> newValues = new LinkedList<Object>(); // 展开集合后新的值
		PreparedStatement ps; // 返回值
		if (values.length == 1) { // 传入1
			Object oValues = values[0];
			if (oValues instanceof Object[]) {
				newSQL = buildSQL(strSQL, (Object[]) oValues, newValues);
			} else if (oValues instanceof JSONArray) { // JSONArray
				newSQL = buildSQL(strSQL, (JSONArray) oValues, newValues);
			} else if (oValues instanceof List<?>) { // List集合
				newSQL = buildSQL(strSQL, (List<Object>) oValues, newValues);
			} else if (oValues instanceof Map<?, ?>) { // JSONObject占位
				newSQL = buildSQL(strSQL, (Map<String, Object>) oValues, newValues);
            } else if (oValues instanceof JSONObject) { // JSONObject占位
                newSQL = buildSQL(strSQL, (JSONObject) oValues, newValues);
			} else { // 将values作为一个对象值传解析
				newSQL = strSQL;
				newValues.add(oValues);
			}
		} else if (values.length > 1) { // 参数数组
			newSQL = buildSQL(strSQL, values, newValues);
		} else { // 没有提供参数
			newSQL = strSQL;
		}
		logger.debug(newSQL);
		// 生成PreparedStatement
		try {
			ps = dbConnection.prepareStatement(newSQL);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		// 使用生成的新值设置
		if (newValues.size() > 0) {
			setValues(ps, newValues);
		}
		return new StatementWrapper(strSQL, newValues, ps);
	}	
	public static StatementWrapper prepareStatement(Connection dbConnection, StatementWrapper qs)  {
		return prepareStatement(dbConnection, qs.getSQL(), qs.getValues());
	}
	/**
	 * 展开传入值集合对象，生成值占位如：?, ?, ?
	 * @param v 传入值，如果是集合，则生成如：?, ?, ?, ?，否则生成：?
	 * @param valuesExpanded 返回展开值
	 * @return 生成预备SQL
	 * @
	 */
	public static String genValueHolder(Object v, List<Object> valuesExpanded) {
		StringBuffer sbSQL = new StringBuffer();
		if (v instanceof Object[]) { // 值为对象数组，展开构建对应的SQL, 下同
			Object[] objArrayValues = (Object[]) v;
			valuesExpanded.addAll(Arrays.asList(objArrayValues));
			String[] vh = new String[objArrayValues.length];
			Arrays.fill(vh, "?");
			sbSQL = new StringBuffer(StringUtils.join(vh, ", "));
		} else if (v instanceof JSONArray) {
			JSONArray jaValues = (JSONArray) v;
			valuesExpanded.addAll(JSONUtils.toMapList(jaValues));
			String[] vh = new String[jaValues.length()];
			Arrays.fill(vh, "?");
			sbSQL = new StringBuffer(StringUtils.join(vh, ", "));
		} else if (v instanceof List<?>) {
			List<Object> listValues = (List<Object>) v;
			valuesExpanded.addAll(listValues);
			String[] vh = new String[listValues.size()];
			Arrays.fill(vh, "?");
			sbSQL = new StringBuffer(StringUtils.join(vh, ", "));
		} else { // 不是集合
			valuesExpanded.add(v);
			sbSQL = new StringBuffer("?");
		}
		return sbSQL.toString();
	}
	/**
	 * 保存JSON对象, JSON属性名和数据库表名相同
	 * @param table, 数据库表名
	 * @param joModel 需要保存的JSONObject
	 * @return
	 * @ 
	 */
	public boolean insert(final String table, final JSONObject joModel) {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = insert(table, joModel);
			}
		});
	}
	/**
	 * 有事务的保存,
	 * @param dbConnection, 数据库连接
	 * @param table, 数据库表名
	 * @param joModel, 要保存的JSONObject
	 * @return
	 * @
	 */
	public boolean insert(Connection dbConnection, String table, JSONObject joModel) {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).insert(table, joModel);
	}
	public void insert(Connection dbConnection, String table, JSONArray jaModels) {
		new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).insert(table, jaModels);
	}
	public void insert(final String table, final JSONArray jaModels) {
		executeTransaction(new ConnectionOperator() {
			@Override
			public void run()  {
				insert(table, jaModels);
			}
		});
	}
	/**
	 * 更新, 不带事务
	 * @param table 数据库表名
	 * @param joModel 要保存的JSONObject
	 * @param where 条件JSONObject, 
	 * @return
	 * @
	 */
	public int update(final String table, final JSONObject joModel, final JSONObject where) {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = update(table, joModel, where);
			}
		});
	}
	/**
	 * 带事务的更新,
	 * @param dbConnection, 数据库连接
	 * @param table, 数据库表名
	 * @param joModel, 要保存的JSONObject
	 * @param where, 条件JSONObject, 包含 first, op, second, 或者字段名, 值 
	 * @return
	 * @
	 */
	public int update(Connection dbConnection, String table, JSONObject joModel, JSONObject where) {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).update(table, joModel, where);
	}
	/**
	 * @param strSQL
	 * @param values 
	 * @return
	 * @
	 */
	public int update(final String strSQL, final Object ... values) {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = update(strSQL, values);
			}
		});
	}
	/**
	 * 带事务的更新, preparedStatement
	 * @param dbConnection 数据库连接
	 * @param strSQL SQL字符串 使用 ? 占位
	 * @param values 需要填充的值, 
	 * @return 
	 * @
	 */
	public int update(Connection dbConnection, final String strSQL, final Object ... values) {
		return new DAOCore(lowerCase, dbConnection, dbTrait, eventHandler).update(strSQL, values);
	}
	/**
	 * @param strSQL
	 * @param values 需要填充的值, 可以是List<Object> 或者 Object[]
	 * @return
	 * @
	 */
	public JSONObject findJSONObject(final boolean lc, final String strSQL, final Object ... values) {
		Map<String, Object> mapRtn = findMap(lc, strSQL, values);
		return mapRtn != null ? new JSONObject(mapRtn) : null;
	}
	/**
	 * @param strSQL
	 * @param values 需要填充的值
	 * @return
	 * @
	 */
	public JSONArray findJSONArray(final boolean lc, 
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return new JSONArray(findMapList(lc, dbConnection, strSQL, values));
	}
	public JSONArray findJSONArray(Connection dbConnection, 
			final String strSQL, 
			final Object ... values) {
		return findJSONArray(lowerCase, dbConnection, strSQL, values);
	}
	public List<Map<String, Object>> findMapList(final boolean lc, 
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return new DAOCore(lc, dbConnection, dbTrait, eventHandler).findMapList(strSQL, values);
	}
	public List<Map<String, Object>> 
		findMapListPage(final boolean lc, 
					Connection dbConnection, 
					String orderCol,
					String direction,
					int start,
					int limit,
					String strSQL, 
					final Object ... values)  {
		return new DAOCore(lc, dbConnection, dbTrait, eventHandler).findMapListPage(orderCol, direction, start, limit, strSQL, values);
	}
	public JSONArray 
		findJSONArrayPage(
			final boolean lc, 
			Connection dbConnection, 
			String orderCol,
			String direction,
			int start,
			int limit,
			String strSQL, 
			final Object ... values)  {
		return new JSONArray(findMapListPage(lc, dbConnection, orderCol, direction, start, limit, strSQL, values));
	}
	public JSONArray 
		findJSONArrayPage(
			Connection dbConnection, 
			String orderCol,
			String direction,
			int start,
			int limit,
			String strSQL, 
			final Object ... values)  {
		return findJSONArrayPage(
				lowerCase, 
				dbConnection, 
				orderCol, 
				direction, 
				start, 
				limit, 
				strSQL, 
				values);
	}
	public JSONArray 
		findJSONArrayPage(
			final boolean lc,
			final String orderCol,
			final String direction,
			final int start,
			final int limit,
			final String strSQL, 
			final Object ... values)  {
		return new JSONArray(findMapListPage(lc, orderCol, direction, start, limit, strSQL, values));
	}
	public JSONArray 
		findJSONArrayPage(
					final String orderCol,
					final String direction,
					final int start,
					final int limit,
					final String strSQL, 
					final Object ... values)  {
		return findJSONArrayPage(lowerCase, 
				orderCol, 
				direction, 
				start, 
				limit, 
				strSQL, 
				values);
	}
	public List<Map<String, Object>> 
		findMapListPage(Connection dbConnection, 
			String orderCol,
			String direction,
			int start,
			int limit,
			String strSQL, 
			final Object ... values)  {
		return findMapListPage(lowerCase, 
				dbConnection, 
				orderCol, 
				direction, 
				start, 
				limit, 
				strSQL, 
				values);
	}
	public List<Map<String, Object>> 
		findMapListPage(final boolean lc, 
			final String orderCol,
			final String direction,
			final int start,
			final int limit,
			final String strSQL, 
			final Object ... values)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = findMapListPage(lc, orderCol, direction, start, limit, strSQL, values);
			}
		});
	}
	public List<Map<String, Object>> 
		findMapListPage(
			final String orderCol,
			final String direction,
			final int start,
			final int limit,
			final String strSQL, 
			final Object ... values)  {
		return  findMapListPage(lowerCase, orderCol, direction, start, limit, strSQL, values);
	}
	public List<Map<String, Object>> 
		findMapList(Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return findMapList(lowerCase, dbConnection, strSQL, values);
	}
	public List<Map<String, Object>> findMapList(final boolean lc, 
			final String strSQL, 
			final Object ... values)  {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = findMapList(lc, strSQL, values);
			}
		});
	}
	public List<Map<String, Object>> findMapList(final String strSQL, 
			final Object ... values)  {
		return findMapList(lowerCase, strSQL, values);
	}
	/**
	 * 查找一个JSONObject
	 * @param lc
	 * @param dbConnection
	 * @param strSQL
	 * @param values
	 * @return 没有查到，返回null
	 * @
	 */
	public JSONObject findJSONObject(boolean lc, 
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		Map<String, Object> mapRtn = findMap(lc, dbConnection, strSQL, values);
		return mapRtn != null ? new JSONObject(mapRtn) : null;
	}
	/**
	 * 查找返回Map对象，没有数据返回null
	 * @param lc
	 * @param dbConnection
	 * @param strSQL
	 * @param values
	 * @return 没有找到返回null
	 * @
	 */
	public Map<String, Object> findMap(boolean lc, 
			Connection dbConnection, 
			final String strSQL, 
			final Object ... values)  {
		return new DAOCore(lc, dbConnection, dbTrait, eventHandler).findMap(strSQL, values);
	}
	public Map<String, Object> findMap(Connection dbConnection, 
			final String strSQL, 
			final Object ... values) {
		return findMap(lowerCase, dbConnection, strSQL, values);
	}
	public Map<String, Object> findMap(final boolean lc, final String strSQL, 
			final Object ... values) {
		return execute(new ConnectionOperator() {
			@Override
			public void run()  {
				result = findMap(lc, strSQL, values);
			}
		});
	}
	public Map<String, Object> findMap(final String strSQL, 
			final Object ... values) {
		return findMap(lowerCase, strSQL, values);
	}
	/**
	 * 根据preparedStringString 查找一个JSONObject
	 * @param strSQL, 如: select * from foo where bar = ?
	 * @param values, 需要填充的值, 可以是List<Object>, Object[], JSONArray
	 * @
     * @return
	 */
	public JSONObject findJSONObject(final String strSQL, final Object ... values) {
		return findJSONObject(lowerCase, strSQL, values);
	}
	/**
	 * 根据preparedStringString 查找一个JSONObject
	 * @param strSQL, 如: select * from foo where bar = ?
	 * @param values 需要填充的值, 可以是List<Object> 或者 Object[]
	 * @
     * @return
	 */
	public JSONObject findJSONObject(Connection dbConnection, 
			final String strSQL, 
			final Object ... values) {
		return findJSONObject(lowerCase, dbConnection, strSQL, values);
	}
	/**
	 * 根据提供的预备SQL, 放置的值, 查找结果为JSONArray 
	 * @param strSQL
	 * 如: select foo, bar from foobar where id = ? and name = ? 查询
	 * @param values, 可以为Object[], List<Object>, JSONArray
	 * @return
	 * @
	 */
	public JSONArray findJSONArray(final String strSQL, final Object ... values) {
		return findJSONArray(lowerCase, strSQL, values);
	}
	/**
	 * 根据提供的预备SQL, 放置的值, 查找结果为JSONArray 
	 * @param strSQL
	 * 如: select foo, bar from foobar where id = ? and name = ? 查询
	 * @param values, 可以为Object[], List<Object>
	 * @return
	 * @
	 */
	public JSONArray findJSONArray(final boolean lc, 
			final String strSQL, 
			final Object ... values)  {
		return new JSONArray(findMapList(lc, strSQL, values));
	}
	public static class DAOCore {
		protected IDbTrait dbTrait;
		protected Connection dbConnection;
		protected IEventHandler event;
		protected boolean lowerCase;

		public DAOCore(boolean lowerCase, Connection dbConnection, IDbTrait dbTrait, IEventHandler event) {
			this.lowerCase = lowerCase;
			this.dbConnection = dbConnection;
			this.dbTrait = dbTrait; 
			this.event = event;
		}
		public DAOCore() {}
		/**
		 * 查找一个String
		 * 
		 * @param dbConnection
		 * @param strSQL
		 * @param values
		 *            <Object[]>, List<Object>, <JSONArray>
		 * @return
		 * @
		 */
		public String findStr(String strSQL, Object... values)
				 {
			return findObj(strSQL, values);
		}

		/**
		 * 查询返回List<String>
		 * 
		 * @param dbConnection
		 * @param strSQL
		 * @param values
		 *            ，可选
		 * @return
		 * @
		 */
		public List<String> findStrList(String strSQL,
				Object... values)  {
			return findObjList(strSQL, values);
		}

		public <T> List<T> findObjList(String strSQL,
				Object... values)  {
			return query(new ResultSetOperator() {
				@Override
				public void run()  {
					List<T> l = new LinkedList<T>();
					try {
						while (resultSet.next())
							l.add((T) resultSet.getObject(1));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					result = l;
				}
			}, strSQL, values);
		}

		public int findInt(String strSQL, Object... values)  {
			return findObj(strSQL, values);
		}
		public long findLong(String strSQL, Object ... values) {
			return findObj(strSQL, values);
		}
		public long findDouble(String strSQL, Object ... values) {
			return findObj(strSQL, values);
		}
		public <T> T findObj(String strSQL, Object... values)  {
			return query(new ResultSetOperator() {
				@Override
				public void run()  {
					try {
						if (resultSet.next())
							result = resultSet.getObject(1);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}, strSQL, values);
		}
		/**
		 * @param dbConnection
		 * @param strSQL
		 * @param values
		 * @param rso
		 * @return
		 * @
		 */
		public <T> T query(ResultSetOperator rso, String strSQL, Object ... values) {
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				StatementWrapper sw = prepareStatement(dbConnection, strSQL, values);
				Event e = null;
				statement = sw.getStatement();
				// 前置事件
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_QUERY)
						.setSQL(sw.getSQL())
						.setValues(sw.getValues());
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				} 
				
				rs = statement.executeQuery();
				if (event != null) {
					event.after(e);
				}
				rso.dbConnection = dbConnection;
				rso.resultSet = rs;
				rso.run();
				return (T) rso.result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				close(statement, rs);
			}
		}

		public boolean execute(String strSQL) {
			PreparedStatement statement = null;
			try {
				logger.debug("execute[" + strSQL + "]");
				
				Event e = null;
				statement = dbConnection.prepareStatement(strSQL);
				// 前置事件
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_EXEC)
						.setSQL(strSQL)
						.setValues(new Object[]{});
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				boolean c = statement.execute();
				if (event != null) {
					event.after(e);
				}
				return c;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement);
			}
		}
		/**
		 * @param dbConnection
		 * @param listSQLs
		 * @
		 */
		public void executeBatch(List<String> listSQLs)  {
			Statement statement = null;
			try {
				statement = dbConnection.createStatement();
				int i = 0;
				for (String sql : listSQLs) {
					statement.addBatch(sql);
					if (++i > batchSize) {
						i = 0;
						statement.executeBatch();
					}
				}
				statement.executeBatch();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement);
			}
		}

		/**
		 * 执行批语句
		 * 
		 * @param strSQL
		 *            , insert into foobar (?, ?)
		 * @param values
		 *            , [[0, 1], [2, 3]]
		 * @
		 */
		public void executeBatch(final String strSQL, final List<Object> values) {
			executeBatch(strSQL, new JSONArray(values));
		}

		/**
		 * 执行批SQL
		 * 
		 * @param dbConnection
		 * @param strSQL
		 *            , insert into foobar (?, ?)
		 * @param jaValues
		 *            , [[1, 2], [3, 4]]
		 * @
		 */
		public void executeBatch(String strSQL, JSONArray jaValues)
				 {
			PreparedStatement preparedStatement = null;
			try {
				logger.debug("execute batch[" + strSQL + "]");
				preparedStatement = dbConnection.prepareStatement(strSQL);
				for (int i = 0; i < jaValues.length(); ++i) {
					setValues(preparedStatement, jaValues
							.getJSONArray(i));
					logger.debug(jaValues.getJSONArray(i));
					preparedStatement.addBatch();
					if (i % batchSize == 0 && i != 0) {
						preparedStatement.executeBatch();
					}
				}
				preparedStatement.executeBatch();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				close(preparedStatement);
			}
		}

		public void insert(String table, JSONArray jaFields, JSONArray jaValues) {
			executeBatch(dbTrait.genInsertSQL(table, jaFields), jaValues);
		}

		/**
		 * 返回表数据行数
		 * @param dbConnection
		 * @param table
		 * @return
		 * @
		 */
		public int count(String table) {
			return findInt("select count(1) from " + table);
		}


		/**
		 * 有事务的保存,
		 * 
		 * @param dbConnection
		 *            , 数据库连接
		 * @param table
		 *            , 数据库表名
		 * @param joModel
		 *            , 要保存的JSONObject
		 * @return
		 * @
		 */
		public boolean insert(String table,
				JSONObject joModel)  {
			PreparedStatement statement = null;
			try {
				List<Object> values = new LinkedList<Object>();
				String insertSQL = dbTrait.genInsertSQL(table, joModel, values);
				logger.debug("new model[" + insertSQL + "]");
				try {
					statement = setValues(dbConnection
							.prepareStatement(insertSQL), values);
					Event e = null;
					if (event != null) {
						e = new Event()
							.setConnection(dbConnection)
							.setStatement(statement)
							.setType(Event.TYPE_INSERT)
							.setSQL(insertSQL)
							.setValues(values);
						if (event.before(e)) {
							statement = e.getStatement();
						} else {
							throw new InterruptException(e.getErrorMsg());
						}
					}
					boolean b = statement.executeUpdate() > 0;
					if (event != null) {
						event.after(e);
					}
					return b;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			} finally {
				close(statement);
			}
		}

		public void insert(String table, JSONArray jaModels)  {
			try {
				for (int i = 0; i < jaModels.length(); ++i) {
					insert(table, jaModels.getJSONObject(i));
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 带事务的更新,
		 * 
		 * @param dbConnection
		 *            , 数据库连接
		 * @param table
		 *            , 数据库表名
		 * @param joModel
		 *            , 要保存的JSONObject
		 * @param where
		 *            , 条件JSONObject, 包含 first, op, second, 或者字段名, 值
		 * @return
		 * @
		 */
		public int update(String table, JSONObject joModel, JSONObject where)  {
			PreparedStatement statement = null;
			try {
				StatementWrapper sw = genUpdateStatement(dbConnection, table, joModel, where);
				statement = sw.getStatement();
				Event e = null;
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_UPDATE)
						.setSQL(sw.getSQL())
						.setValues(sw.getValues());
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				int c = statement.executeUpdate();
				if (event != null) {
					event.after(e);
				}
				return c;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement);
			}
		}

		/**
		 * 带事务的更新, preparedStatement
		 * 
		 * @param dbConnection
		 *            数据库连接
		 * @param strSQL
		 *            SQL字符串 使用 ? 占位
		 * @param values
		 *            需要填充的值,
		 * @return
		 * @
		 */
		public int update(final String strSQL, final Object... values)  {
			PreparedStatement statement = null;
			try {
				StatementWrapper sw = prepareStatement(dbConnection, strSQL, values);
				statement = sw.getStatement();
				Event e = null;
				// 前置事件
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_UPDATE)
						.setSQL(sw.getSQL())
						.setValues(values);
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				int c = statement.executeUpdate();
				if (event != null) {
					event.after(e);
				}
				return c;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement);
			}
		}

		/**
		 * @param strSQL
		 * @param values
		 *            需要填充的值
		 * @return
		 * @
		 */
		public JSONArray findJSONArray(final boolean lc,
				final String strSQL,
				final Object... values)  {
			return new JSONArray(findMapList(lc, strSQL,
					values));
		}

		public JSONArray findJSONArray(
				final String strSQL, final Object... values)  {
			return findJSONArray(lowerCase, strSQL,
					values);
		}

		public List<Map<String, Object>> findMapList(final boolean lc,
				final String strSQL,
				final Object... values)  {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				StatementWrapper sw = prepareStatement(dbConnection, strSQL, values);
				statement = sw.getStatement();
				Event e = null;
				// 前置事件
				if (event != null) {
					e = new Event()
					.setConnection(dbConnection)
					.setStatement(statement)
					.setType(Event.TYPE_QUERY)
					.setSQL(sw.getSQL());
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				resultSet = statement.executeQuery();
				if (event != null) {
					event.after(e);
				}
				return getMapList(resultSet, lc);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement, resultSet);
			}
		}

		public List<Map<String, Object>> findMapListPage(final boolean lc,
				String orderCol, String direction,
				int start, int limit, String strSQL, final Object... values)
				 {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				
				StatementWrapper sw = prepareStatement(dbConnection, dbTrait
						.pageStatement(orderCol, direction, start, limit,
								strSQL, values));
				statement = sw.getStatement();
				Event e = null;
				// 前置事件
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_QUERY)
						.setSQL(sw.getSQL())
						.setValues(sw.getValues());
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				resultSet = statement.executeQuery();
				
				if (event != null) {
					event.after(e);
				}
				return getMapList(resultSet, lc);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement, resultSet);
			}
		}

		public JSONArray findJSONArrayPage(final boolean lc,
				 String orderCol, String direction,
				int start, int limit, String strSQL, final Object... values)
				 {
			return new JSONArray(findMapListPage(lc, orderCol,
					direction, start, limit, strSQL, values));
		}

		public JSONArray findJSONArrayPage(
				String orderCol, String direction, int start, int limit,
				String strSQL, final Object... values)  {
			return findJSONArrayPage(lowerCase, 
					orderCol, direction, start, limit, strSQL, values);
		}

		public List<Map<String, Object>> findMapListPage(
				String orderCol, String direction, int start, int limit,
				String strSQL, final Object... values)  {
			return findMapListPage(lowerCase, orderCol,
					direction, start, limit, strSQL, values);
		}

		public List<Map<String, Object>> findMapList(
				final String strSQL, final Object... values)  {
			return findMapList(lowerCase, strSQL,
					values);
		}

		/**
		 * 查找一个JSONObject
		 * 
		 * @param lowerCase
		 * @param dbConnection
		 * @param strSQL
		 * @param values
		 * @return 没有查到，返回null
		 * @
		 */
		public JSONObject findJSONObject(boolean lc,
				final String strSQL,
				final Object... values)  {
			Map<String, Object> mapRtn = findMap(lc, strSQL, values);
			return mapRtn != null ? new JSONObject(mapRtn) : null;
		}

		/**
		 * 查找返回Map对象，没有数据返回null
		 * 
		 * @param lowerCase
		 * @param dbConnection
		 * @param strSQL
		 * @param values
		 * @return 没有找到返回null
		 * @
		 */
		public Map<String, Object> findMap(boolean lc,
				final String strSQL, final Object... values)  {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			Map<String, Object> mapRtn = null;
			try {
				StatementWrapper sw = prepareStatement(dbConnection, strSQL, values); 
				statement = sw.getStatement();
				Event e = null;
				// 前置事件
				if (event != null) {
					e = new Event()
						.setConnection(dbConnection)
						.setStatement(statement)
						.setType(Event.TYPE_QUERY)
						.setSQL(sw.getSQL())
						.setValues(sw.getValues());
					if (event.before(e)) {
						statement = e.getStatement();
					} else {
						throw new InterruptException(e.getErrorMsg());
					}
				}
				resultSet = statement.executeQuery();
				if (event != null) {
					event.after(e);
				}
				if (resultSet.next())
					mapRtn = getMap(resultSet, lc);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(statement, resultSet);
			}
			return mapRtn;
		}

		public Map<String, Object> findMap(final String strSQL,
				final Object... values)  {
			return findMap(lowerCase, strSQL, values);
		}

		/**
		 * 根据preparedStringString 查找一个JSONObject
		 * 
		 * @param strSQL
		 *            , 如: select * from foo where bar = ?
		 * @param values
		 *            需要填充的值, 可以是List<Object> 或者 Object[]
		 * @
		 * @return
		 */
		public JSONObject findJSONObject(final String strSQL, final Object... values)  {
			return findJSONObject(lowerCase,
					strSQL, values);
		}
		/**
		 * 生成更新预备SQL
		 * @param dbConnection
		 * @param table
		 * @param joModel
		 * @param objWhere 条件对象
		 * @return 已设值的statement
		 * @
		 */
		protected StatementWrapper genUpdateStatement(Connection dbConnection,
				String table, JSONObject joModel, 
				JSONObject joWhere) {
			
			List<Object> values = new LinkedList<Object>();
			StringBuffer sbSQL = 
				new StringBuffer(dbTrait.genUpdateSetSQL(table, joModel, values))
				.append(" where ").append(dbTrait.genWhereEqAnd(joWhere, values));
			logger.debug("Gen Update SQL");
			logger.debug(sbSQL.toString());
			try {
				StatementWrapper sw = new StatementWrapper(sbSQL.toString(), values);
				return sw.setStatement(setValues(dbConnection.prepareStatement(sbSQL.toString()), values));
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
