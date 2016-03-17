package me.chyxion.dao.pagination;

import me.chyxion.dao.Order;
import me.chyxion.dao.SqlAndArgs;
import me.chyxion.dao.utils.StringUtils;

import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 10, 2015 10:15:52 PM
 */
public class MySQLCompatiblePaginationProcessor extends PaginationProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlAndArgs processPaginationSqlAndArgs(Collection<Order> orders,
			int start, int limit, String sql, Collection<? super Object> args) {
		sql += " order by " + StringUtils.join(orders, ", ") + " limit " + start;
		if (limit > 0) {
			sql += ", " + limit;
		}
		return new SqlAndArgs(sql, args);
	}
}
