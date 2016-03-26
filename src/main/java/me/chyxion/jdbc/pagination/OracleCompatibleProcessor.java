package me.chyxion.jdbc.pagination;

import org.slf4j.Logger;

import java.util.Collection;
import org.slf4j.LoggerFactory;

import me.chyxion.jdbc.Order;
import me.chyxion.jdbc.SqlAndArgs;
import me.chyxion.jdbc.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 10, 2015 10:29:47 PM
 */
public class OracleCompatibleProcessor 
	implements PaginationProcessor {
	private static final Logger log = 
		LoggerFactory.getLogger(OracleCompatibleProcessor.class);

	/**
	 	<pre>
		SELECT * FROM (
			SELECT ROW_NUMBER() OVER (ORDER BY OrderDate) RowNum, *
			FROM  Orders
			WHERE OrderDate &gt;= '1980-01-01') RowConstrainedResult
		WHERE RowNum &gt;= 1
		    AND RowNum &lt; 20
		ORDER BY RowNum
		</pre>
	 * {@inheritDoc}
	 */
	public SqlAndArgs process(
			Collection<Order> orders,
			int start, 
			int limit,
			String sql, 
			Collection<? super Object> args) {
		log.info("Process Oracle Compatible Pagination Sql [{}].", sql);
		int indexFrom = StringUtils.indexOfIgnoreCase(sql, " from ");
		
		StringBuilder sbSql = 
			new StringBuilder("select * from (")
			.append(sql.substring(0, indexFrom))
			.append(", row_number() over (order by ")
			.append(StringUtils.join(orders, ", "))
			.append(") ")
			.append(COLUMN_ROW_NUMBER)
			.append(sql.substring(indexFrom))
			.append(") where ")
			.append(COLUMN_ROW_NUMBER)
			.append(" >= ? "); 
		args.add(start);
		if (limit > 0) {
			sbSql.append("and ")
			.append(COLUMN_ROW_NUMBER)
			.append(" < ?");
			args.add(start + limit);
		} 
		log.info("Process Pagination Sql Result [{}].", sql);
		return new SqlAndArgs(sbSql.toString(), args);
	}
}
