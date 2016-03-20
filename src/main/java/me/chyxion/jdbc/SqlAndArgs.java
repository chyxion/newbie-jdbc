package me.chyxion.jdbc;

import java.util.Collection;

/**
 * SQL And Args
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Mar 20, 2016 1:43:19 PM
 */
public class SqlAndArgs {
	private String sql;
	private Collection<?> args;

	/**
	 * @param sql sql
	 * @param args sql args
	 */
	public SqlAndArgs(String sql, Collection<?> args) {
		this.sql = sql;
		this.args = args;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @return the args
	 */
	public Collection<?> getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Collection<?> args) {
		this.args = args;
	}
}
