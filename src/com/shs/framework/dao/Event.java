package com.shs.framework.dao;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
/**
 * @version 0.1
 * @author chyxion
 * @describe: 数据库操作事件
 * @date created: Jul 23, 2013 10:28:58 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by:
 */
public class Event {
	/**
	 * 查询
	 */
	public final static int TYPE_QUERY = 0;
	/**
	 * 插入
	 */
	public final static int TYPE_INSERT = 1;
	/**
	 * 更新
	 */
	public final static int TYPE_UPDATE = 2;
	/**
	 * 执行
	 */
	public final static int TYPE_EXEC = 3;
	/**
	 * 执行批处理
	 */
	public final static int TYPE_EXEC_BATCH = 3;

	private Connection dbConnection;
	private Statement statement;
	private int type;
	private String sql;
	private Object[] values;
	private String errorMsg;

	public Connection getConnection() {
		return dbConnection;
	}
	public Event setConnection(Connection dbConnection) {
		this.dbConnection = dbConnection;
		return this;
	}
	/**
	 * 有可能传入的是PreparedStatement，这里做动态转换
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getStatement() {
		return (T) statement;
	}
	public Event setStatement(Statement statement) {
		this.statement = statement;
		return this;
	}
	public int getType() {
		return type;
	}
	public Event setType(int type) {
		this.type = type;
		return this;
	}
	public String getSQL() {
		return sql;
	}
	public Event setSQL(String sql) {
		this.sql = sql;
		return this;
	}
	public Object[] getValues() {
		return values;
	}
	public Event setValues(List<Object> values) {
		this.values = values.toArray();
		return this;
	}
	public Event setValues(Object[] values) {
		this.values = values;
		return this;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
}
