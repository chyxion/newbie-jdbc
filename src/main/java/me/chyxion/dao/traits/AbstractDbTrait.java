package me.chyxion.dao.traits;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import me.chyxion.dao.Order;
import me.chyxion.dao.po.SqlAndArgs;
import me.chyxion.dao.utils.StringUtils;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
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
    public abstract SqlAndArgs pageStatement(
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
				.append(StringUtils.join(Arrays.asList(vh), ", "))
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
			.append(StringUtils.join(Arrays.asList(vh), ", "))
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

    public static AbstractDbTrait getDbType(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }
        return null;
/*
        else if (rawUrl.startsWith("jdbc:mysql:") || rawUrl.startsWith("jdbc:cobar:")) {
            return new MySQLTrait();
        } 
        else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
            return LOG4JDBC;
        } else if (rawUrl.startsWith("jdbc:mariadb:")) {
            return MARIADB;
        } else if (rawUrl.startsWith("jdbc:oracle:")) {
            return ORACLE;
        } else if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
            return ALI_ORACLE;
        } else if (rawUrl.startsWith("jdbc:microsoft:")) {
            return SQL_SERVER;
        } else if (rawUrl.startsWith("jdbc:sqlserver:")) {
            return SQL_SERVER;
        } else if (rawUrl.startsWith("jdbc:sybase:Tds:")) {
            return SYBASE;
        } else if (rawUrl.startsWith("jdbc:jtds:")) {
            return JTDS;
        } else if (rawUrl.startsWith("jdbc:fake:") || rawUrl.startsWith("jdbc:mock:")) {
            return MOCK;
        } else if (rawUrl.startsWith("jdbc:postgresql:")) {
            return POSTGRESQL;
        } else if (rawUrl.startsWith("jdbc:hsqldb:")) {
            return HSQL;
        } else if (rawUrl.startsWith("jdbc:db2:")) {
            return DB2;
        } else if (rawUrl.startsWith("jdbc:sqlite:")) {
            return "sqlite";
        } else if (rawUrl.startsWith("jdbc:ingres:")) {
            return "ingres";
        } else if (rawUrl.startsWith("jdbc:h2:")) {
            return H2;
        } else if (rawUrl.startsWith("jdbc:mckoi:")) {
            return "mckoi";
        } else if (rawUrl.startsWith("jdbc:cloudscape:")) {
            return "cloudscape";
        } else if (rawUrl.startsWith("jdbc:informix-sqli:")) {
            return "informix";
        } else if (rawUrl.startsWith("jdbc:timesten:")) {
            return "timesten";
        } else if (rawUrl.startsWith("jdbc:as400:")) {
            return "as400";
        } else if (rawUrl.startsWith("jdbc:sapdb:")) {
            return "sapdb";
        } else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
            return "JSQLConnect";
        } else if (rawUrl.startsWith("jdbc:JTurbo:")) {
            return "JTurbo";
        } else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
            return "firebirdsql";
        } else if (rawUrl.startsWith("jdbc:interbase:")) {
            return "interbase";
        } else if (rawUrl.startsWith("jdbc:pointbase:")) {
            return "pointbase";
        } else if (rawUrl.startsWith("jdbc:edbc:")) {
            return "edbc";
        } else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
            return "mimer";
        } else if (rawUrl.startsWith("jdbc:dm:")) {
            return JdbcConstants.DM;
        } else if (rawUrl.startsWith("jdbc:kingbase:")) {
            return JdbcConstants.KINGBASE;
        } else {
            return null;
        }
        */
    }
}
