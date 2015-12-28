package me.chyxion.dao.traits;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import me.chyxion.dao.Order;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 10, 2015 10:29:47 PM
 */
public class OracleTrait extends AbstractDbTrait {

	/**
	 * 
		SELECT  *
		FROM  (SELECT    ROW_NUMBER() OVER ( ORDER BY OrderDate) RowNum, *
		          FROM      Orders
		          WHERE     OrderDate >= '1980-01-01') RowConstrainedResult
		WHERE   RowNum >= 1
		    AND RowNum < 20
		ORDER BY RowNum
	 * @param orderCol
	 * @param direction
	 * @param start
	 * @param limit
	 * @param sql
	 * @param values
	 * @return
	 */
	@Override
	public Pair<String, Collection<? extends Object>> pageStatement(
			Collection<Order> orders,
			int start, 
			int limit,
			String sql, 
			Collection<? super Object> values) {
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
		values.add(start);
		if (limit > 0) {
			sbSql.append(" and ")
			.append(COLUMN_ROW_NUMBER)
			.append(" < ? ");
			values.add(start + limit);
		} 
		return new ImmutablePair<String, 
			Collection<? extends Object>>(sbSql.toString(), values);
	}
}
