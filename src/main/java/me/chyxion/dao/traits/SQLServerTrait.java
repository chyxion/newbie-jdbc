package me.chyxion.dao.traits;

import java.util.Collection;
import me.chyxion.dao.Order;
import me.chyxion.dao.po.SqlAndArgs;
import me.chyxion.dao.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Dec 10, 2015 10:41:57 PM
 */
public class SQLServerTrait extends AbstractDbTrait {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlAndArgs pageStatement(
		Collection<Order> orders, int offset, int limit, String strSQL, Collection<? super Object> values) {
//		SELECT  *
//		FROM  ( SELECT    ROW_NUMBER() OVER ( ORDER BY OrderDate ) AS RowNum, *
//		          FROM      Orders
//		          WHERE     OrderDate >= '1980-01-01'
//		        ) AS RowConstrainedResult
//		WHERE   RowNum >= 1
//		    AND RowNum < 20
//		ORDER BY RowNum
		int indexFrom = StringUtils.indexOfIgnoreCase(strSQL, " from ");
		StringBuilder sbSQL = 
			new StringBuilder("select * from (")
			.append(strSQL.substring(0, indexFrom))
			.append(", row_number() over (order by ")
			.append(StringUtils.join(orders, ", "))
			.append(") ")
			.append(COLUMN_ROW_NUMBER)
			.append(strSQL.substring(indexFrom))
			.append(") temp_result__ where ")
			.append(COLUMN_ROW_NUMBER)
			.append(" >= ? "); 
		values.add(offset);
		if (limit > 0) {
			sbSQL.append("and ")
			.append(COLUMN_ROW_NUMBER)
			.append(" < ? ");
			values.add(offset + limit);
		} 
		return new SqlAndArgs(sbSQL.toString(), values);
	}
}
