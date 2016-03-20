package me.chyxion.jdbc.pagination;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.chyxion.jdbc.Order;
import me.chyxion.jdbc.SqlAndArgs;
import me.chyxion.jdbc.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 10, 2015 10:15:52 PM
 */
public class MySQLCompatiblePaginationProcessor 
	implements PaginationProcessor {
	private static final Logger log = 
		LoggerFactory.getLogger(MySQLCompatiblePaginationProcessor.class);

	/**
	 * {@inheritDoc}
	 */
	public SqlAndArgs process(Collection<Order> orders,
			int start, int limit, String sql, Collection<? super Object> args) {
		log.info("Process MySQL Compatible Pagination Sql [{}].", sql);
		sql += " order by " + StringUtils.join(orders, ", ") + " limit ?";
		args.add(start);
		if (limit > 0) {
			sql += ", ?";
			args.add(limit);
		}
		log.info("Process Pagination Sql Result [{}].", sql);
		return new SqlAndArgs(sql, args);
	}
}
