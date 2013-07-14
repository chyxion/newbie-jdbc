package com.shs.framework.dao.traits;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * @version 0.1
 * @author chyxion
 * @describe: 查询语句
 * @date created: Jan 24, 2013 12:40:36 PM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 * @copyright: Shenghang Soft All Right Reserved.
 */
public class StatementWrapper {
	private String strSQL;
	private Object[] values;
	private PreparedStatement statement;
	
	public StatementWrapper setValues(Object[] values) {
		this.values = values;
		return this;
	}
	public Object[] getValues() {
		return values;
	}
	public StatementWrapper setSQL(String strSQL) {
		this.strSQL = strSQL;
		return this;
	}
	public String getSQL() {
		return strSQL;
	}
	public StatementWrapper(String strSQL, Object ... values) {
		this.strSQL = strSQL;
		this.values = values;
	}
	public StatementWrapper(String strSQL, List<Object> values) {
		this.strSQL = strSQL;
		this.values = values.toArray();
	}
	public StatementWrapper(String strSQL, List<Object> values, PreparedStatement ps) {
		this.strSQL = strSQL;
		this.values = values.toArray();
		this.setStatement(ps);
	}
	public StatementWrapper setStatement(PreparedStatement statement) {
		this.statement = statement;
		return this;
	}
	public PreparedStatement getStatement() {
		return statement;
	}
}
