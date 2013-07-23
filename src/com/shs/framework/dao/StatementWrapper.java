package com.shs.framework.dao;
import java.sql.PreparedStatement;
import java.util.List;
/**
 * @version 0.1
 * @author chyxion
 * @describe: Statement包装器
 * @date created: Jul 23, 2013 10:31:31 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by:
 */
public class StatementWrapper {
	private PreparedStatement statement;
	private String sql;
	private List<Object> values;
	
	public PreparedStatement getStatement() {
		return statement;
	}
	public void setStatement(PreparedStatement statement) {
		this.statement = statement;
	}
	public String getSQL() {
		return sql;
	}
	public void setSQL(String sql) {
		this.sql = sql;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public StatementWrapper() {
	}
	public StatementWrapper(PreparedStatement statement, String sql,
			List<Object> values) {
		super();
		this.statement = statement;
		this.sql = sql;
		this.values = values;
	}
}
