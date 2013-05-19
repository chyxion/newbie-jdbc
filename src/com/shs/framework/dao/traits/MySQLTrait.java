package com.shs.framework.dao.traits;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySQLTrait extends IDbTrait {
	@Override
	public QueryStatement pageStatement(String orderCol, String direction,
			int start, int limit, String strSQL, Object... values) {
		strSQL += " order by `" + orderCol + "` " + direction + " limit " + start;
		if (limit > 0) 
			strSQL += ", " + limit;
		return new QueryStatement(strSQL, values);
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
		return new StringBuffer("insert into `")
				.append(table)
				.append("` (`")
				.append(StringUtils.join(columns, "`, `"))
				.append("`) values (")
				.append(StringUtils.join(vh, ", "))
				.append(")").toString();
	}
	@Override
	public String genInsertSQL(String table, JSONArray jaFields)  {
		String[] vh = new String[jaFields.length()];
		Arrays.fill(vh, "?");
		try {
			return new StringBuffer("inser into `").append(table).append("` (`")
					.append(jaFields.join("`, `")).append("`) values (")
					.append(StringUtils.join(vh, ", ")).append(")").toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String genUpdateSetSQL(String table, JSONObject joModel, List<Object> values) {
		StringBuffer sbSQL = 
				new StringBuffer("update `")
					.append(table)
					.append("` set ");
		String[] columns = JSONObject.getNames(joModel);
		try {
			for (String col : columns) {
				sbSQL.append("`")
					.append(col).append("` = ?, ");
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
				sbSQL.append("`")
					.append(column).append("` = ? and ");
				outValues.add(joWhere.get(column));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		sbSQL.setLength(sbSQL.length() - 5); // 去掉and
		return sbSQL.toString();
	}
}
