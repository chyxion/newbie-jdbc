package com.shs.framework.dao.traits;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @version 0.1
 * @author chyxion
 * @describe: oracle数据库特征
 * @date created: Jan 24, 2013 11:38:15 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 * @copyright: Shenghang Soft All Right Reserved.
 */
public class OracleTrait extends IDbTrait {
	/**
	 * 分页语句
		SELECT  *
		FROM  ( SELECT    ROW_NUMBER() OVER ( ORDER BY OrderDate) RowNum, *
		          FROM      Orders
		          WHERE     OrderDate >= '1980-01-01'
		        ) RowConstrainedResult
		WHERE   RowNum >= 1
		    AND RowNum < 20
		ORDER BY RowNum
	 */
	@Override
	public StatementWrapper pageStatement(
			String orderCol,
			String direction,
			int start, 
			int limit,
			String strSQL, 
			Object ... values) {
		int indexFrom = StringUtils.indexOfIgnoreCase(strSQL, " FROM ");
		
		if (orderCol.contains(".")) {
			orderCol = orderCol.replaceAll("\\.", "\".\"");
		}
		orderCol = orderCol.toUpperCase();
		
		StringBuffer sbSQL = 
			new StringBuffer("SELECT * FROM (")
			.append(strSQL.substring(0, indexFrom))
			.append(", ROW_NUMBER() OVER (ORDER BY \"")
			.append(orderCol)
			.append("\" ")
			.append(direction)
			.append(") ")
			.append(COLUMN_ROW_NUMBER)
			.append(strSQL.substring(indexFrom))
			.append(") WHERE ")
			.append(COLUMN_ROW_NUMBER)
			.append(" >= ? "); 
		if (limit > 0) {
			sbSQL.append(" AND ")
			.append(" <= ? ");
			values = ArrayUtils.addAll(values, new Object[]{start, start + limit});
		} else {
			values = ArrayUtils.add(values, start);
		}
		return new StatementWrapper(sbSQL.toString(), values);
	}

	@Override
	public String genInsertSQL(String table, JSONObject joModel, List<Object> values)  {
		// 获得对象属性名称
		String[] columns = JSONObject.getNames(joModel);
		String[] vh = new String[joModel.length()];
		Arrays.fill(vh, "?");
		try {
			// 遍历列
			for (String column : columns) {
				values.add(joModel.get(column)); // 添加值
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new StringBuffer("INSERT INTO \"")
				.append(table.toUpperCase())
				.append("\" (\"")
				.append(StringUtils.join(columns, "\", \"").toUpperCase())
				.append("\") VALUES (")
				.append(StringUtils.join(vh, ", "))
				.append(")").toString();
	}
	@Override
	public String genInsertSQL(String table, JSONArray jaFields)  {
		String[] vh = new String[jaFields.length()];
		Arrays.fill(vh, "?");
		try {
			return new StringBuffer("INSER INTO \"").append(table.toUpperCase()).append("\" (\"")
					.append(jaFields.join("\", \"")).append("\") VALUES (")
					.append(StringUtils.join(vh, ", ")).append(")").toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String genUpdateSetSQL(String table, JSONObject joModel, List<Object> values) {
		StringBuffer sbSQL = 
				new StringBuffer("UPDATE \"")
					.append(table)
					.append("\" SET ");
		String[] columns = JSONObject.getNames(joModel);
		try {
			for (String col : columns) {
				sbSQL.append("\"")
					.append(col.toUpperCase()).append("\" = ?, ");
				values.add(joModel.get(col));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		sbSQL.setLength(sbSQL.length() - 2);
		return sbSQL.toString();
	}
	@Override
	public String genWhereEqAnd(JSONObject joWhere, List<Object> outValues) {
		StringBuffer sbSQL = new StringBuffer();
		String[] columns = JSONObject.getNames(joWhere);
		try {
			for (String column : columns) {
				sbSQL.append("\"")
					.append(column.toUpperCase())
					.append("\" = ? AND ");
				outValues.add(joWhere.get(column));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		sbSQL.setLength(sbSQL.length() - 5); // 去掉and
		return sbSQL.toString();
	}
}
