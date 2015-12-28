package me.chyxion.dao.traits;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import me.chyxion.dao.Order;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br />
 * chyxion@163.com <br />
 * Dec 10, 2015 10:08:05 PM
 */
public abstract class AbstractDbTrait {
	/**
	 * row number for pagination
	 */
	public static final String COLUMN_ROW_NUMBER = "row_number__";

	/**
	 * @param orderCol
	 * @param direction
	 * @param offset
	 * @param limit
	 * @param sql
	 * @param args
	 * @return
	 */
    public abstract Pair<String, Collection<? extends Object>> pageStatement(
    		Collection<Order> orders,
    		int offset, 
    		int limit,
    		String sql, 
    		Collection<? super Object> args);

    /**
     * generate insert SQL
     * @param table
     * @param mapModel
     * @param values
     * @return
     */
	public CharSequence genInsertSQL(String table, 
		Map<String, Object> mapModel, List<Object> values)  {

		Set<String> columns = mapModel.keySet();
		String[] vh = new String[mapModel.size()];
		Arrays.fill(vh, "?");
		for (String column : columns) {
			values.add(mapModel.get(column)); // 添加值
		}
		return new StringBuilder("insert into ")
				.append(table)
				.append(" (")
				.append(StringUtils.join(columns, ", "))
				.append(") values (")
				.append(StringUtils.join(vh, ", "))
				.append(")");
	}

	/**
	 * @param table
	 * @param jaFields
	 * @return
	 */
	public CharSequence genInsertSQL(String table, List<String> jaFields)  {
		String[] vh = new String[jaFields.size()];
		Arrays.fill(vh, "?");
		return new StringBuffer("inser into ")
			.append(table)
			.append(" (")
			.append(StringUtils.join(jaFields, ", "))
			.append(") values (")
			.append(StringUtils.join(vh, ", "))
			.append(")");
	}

	/**
	 * @param table
	 * @param mapModel
	 * @param values
	 * @return
	 */
	public CharSequence genUpdateSetSQL(String table, 
		Map<String, Object> mapModel, List<Object> values) {
		StringBuilder sbSQL = 
				new StringBuilder("update ")
					.append(table)
					.append(" set ");
		for (String col : mapModel.keySet()) {
			sbSQL.append(col).append(" = ?, ");
			values.add(mapModel.get(col));
		}
		// remove last [, ]
		sbSQL.setLength(sbSQL.length() - 2);
		return sbSQL;
	}

	/**
	 * {"a": 1, "b": 2} => 
	 * 	return a = ? and b = ?
	 * 	outValues == [1, 2]
	 * @param mapWhere
	 * @param outValues
	 * @return
	 */
	public CharSequence genWhereEqAnd(
		Map<String, Object> mapWhere, List<Object> outValues) {
		StringBuilder sbSQL = new StringBuilder();
		for (String name : mapWhere.keySet()) {
			sbSQL.append(name).append(" = ? and ");
			outValues.add(mapWhere.get(name));
		}
		// remove last [ and ]
		sbSQL.setLength(sbSQL.length() - 5);
		return sbSQL;
	}
}
