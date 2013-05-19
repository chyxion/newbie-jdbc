package com.shs.framework.dao.traits;

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
public class QueryStatement {
	private String strSQL;
	private Object[] values;
	public void setValues(Object[] values) {
		this.values = values;
	}
	public Object[] getValues() {
		return values;
	}
	public void setStrSQL(String strSQL) {
		this.strSQL = strSQL;
	}
	public String getStrSQL() {
		return strSQL;
	}
	public QueryStatement(String strSQL, Object ... values) {
		this.strSQL = strSQL;
		this.values = values;
	}
}
