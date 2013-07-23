package com.shs.framework.dao.traits;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @version 0.1
 * @author chyxion
 * @describe: 数据库特征
 * @date created: Jan 24, 2013 11:35:36 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 */
public abstract class IDbTrait {
	/**
	 * 分页时候行号的Label
	 */
	public static final String COLUMN_ROW_NUMBER = "row_number__";
    public abstract StatementWrapper pageStatement(
    		String orderCol, 
    		String direction, 
    		int start, 
    		int limit,
    		String strSQL, 
    		Object ... values);
    /**
     * 生成插入SQL
     * @param table
     * @param joModel
     * @param values
     * @return
     */
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
		return new StringBuffer("insert into ")
				.append(table)
				.append(" (")
				.append(StringUtils.join(columns, ", "))
				.append(") values (")
				.append(StringUtils.join(vh, ", "))
				.append(")").toString();
	}
	public String genInsertSQL(String table, JSONArray jaFields)  {
		String[] vh = new String[jaFields.length()];
		Arrays.fill(vh, "?");
		try {
			return new StringBuffer("inser into ").append(table).append(" (")
					.append(jaFields.join(", ")).append(") values (")
					.append(StringUtils.join(vh, ", ")).append(")").toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	public String genUpdateSetSQL(String table, JSONObject joModel, List<Object> values) {
		StringBuffer sbSQL = 
				new StringBuffer("update ")
					.append(table)
					.append(" set ");
		String[] columns = JSONObject.getNames(joModel);
		try {
			for (String col : columns) {
				sbSQL.append(col).append(" = ?, ");
				values.add(joModel.get(col));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		sbSQL.setLength(sbSQL.length() - 2);
		return sbSQL.toString();
	}
	/**
	 * 生成and条件，如：{"a": 1, "b": 2} => 
	 * 	return a = ? and b = ?
	 * 	outValues == [1, 2]
	 * @param joWhere
	 * @param outValues
	 * @return
	 */
	public String genWhereEqAnd(JSONObject joWhere, List<Object> outValues) {
		StringBuffer sbSQL = new StringBuffer();
		try {
			for (String name : JSONObject.getNames(joWhere)) {
				sbSQL.append(name).append(" = ? and ");
				outValues.add(joWhere.get(name));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		sbSQL.setLength(sbSQL.length() - 5); // 去掉最后一个and
		return sbSQL.toString();
	}
}
