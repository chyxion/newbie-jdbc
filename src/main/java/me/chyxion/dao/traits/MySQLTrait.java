package me.chyxion.dao.traits;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import me.chyxion.dao.Order;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 10, 2015 10:15:52 PM
 */
public class MySQLTrait extends AbstractDbTrait {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Collection<? extends Object>> pageStatement(Collection<Order> orders,
			int start, int limit, String sql, Collection<? super Object> args) {
		sql += " order by " + StringUtils.join(orders, ", ") + " limit " + start;
		if (limit > 0) {
			sql += ", " + limit;
		}
		return new ImmutablePair<String, Collection<? extends Object>>(sql, args);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence genInsertSQL(String table, 
		Map<String, Object> joModel, List<Object> values)  {
		// 获得对象属性名称
		Set<String> columns = joModel.keySet();
		String[] vh = new String[joModel.size()];
		Arrays.fill(vh, "?");
		for (String column : columns) {
			values.add(joModel.get(column)); // 添加值
		}
		return new StringBuffer("insert into `")
				.append(table)
				.append("` (`")
				.append(StringUtils.join(columns, "`, `"))
				.append("`) values (")
				.append(StringUtils.join(vh, ", "))
				.append(")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence genInsertSQL(String table, List<String> fields)  {
		String[] vh = new String[fields.size()];
		Arrays.fill(vh, "?");
		return new StringBuilder("inser into `")
			.append(table).append("` (`")
			.append(StringUtils.join(fields, "`, `"))
			.append("`) values (")
			.append(StringUtils.join(vh, ", "))
			.append(")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence genUpdateSetSQL(
		String table, Map<String, Object> joModel, List<Object> values) {
		StringBuilder sbSQL = 
			new StringBuilder("update `")
				.append(table)
				.append("` set ");
		Set<String> columns = joModel.keySet();
		for (String col : columns) {
			sbSQL.append("`")
				.append(col)
				.append("` = ?, ");
			values.add(joModel.get(col));
		}
		// remove last [, ]
		sbSQL.setLength(sbSQL.length() - 2);
		return sbSQL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence genWhereEqAnd(
		Map<String, Object> mapWhere, List<Object> outValues) {
		StringBuilder sbSQL = new StringBuilder();
		Set<String> columns = mapWhere.keySet();
		for (String column : columns) {
			sbSQL.append("`")
				.append(column).append("` = ? and ");
			outValues.add(mapWhere.get(column));
		}
		// remove last [ and ]
		sbSQL.setLength(sbSQL.length() - 5);
		return sbSQL;
	}
}
