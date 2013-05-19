package com.shs.framework.dao;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.shs.framework.dao.traits.MySQLTrait;
import com.shs.framework.dao.traits.OracleTrait;
import com.shs.framework.dao.traits.SQLServerTrait;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @class describe: 数据连接管理器
 * @version 0.1
 * @date created: Jun 25, 2012 3:08:58 PM
 * @author chyxion
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 * @copyright: Shenghang Soft All Right Reserved.
 */
public class ConnectionManager {
	/**
	 * 数据库方言
	 */
	public static String DIALECT = "oracle";
	/**
	 * 数据库连接驱动
	 */
	public static String DRIVER = "oracle.jdbc.OracleDriver";
	/**
	 * 连接URL
	 */
	public static String URL = "jdbc:oracle:thin:@192.168.1.168:1521:oracle";
	/**
	 * 用户名
	 */
	public static String USER_NAME = "idps_admin";
	/**
	 * 密码
	 */
	public static String PASSWORD = "idps0211";
	/**
	 * 数据源名称
	 */
	public static String DATA_SOURCE_NAME;
	public static IDataSourceProvider dataSourceProvider;
	public static void setDataSourceProvider(IDataSourceProvider dsp) {
		dataSourceProvider = dsp;
	}
	public static void setDialect(String dialect) {
		DIALECT = dialect;
		if ("oracle".equalsIgnoreCase(dialect)) {
			BaseDAO.setDbTrait(new OracleTrait());
		} else if ("sqlserver".equalsIgnoreCase(dialect)) {
			BaseDAO.setDbTrait(new SQLServerTrait());
		} else if ("mysql".equalsIgnoreCase(dialect)) {
			BaseDAO.setDbTrait(new MySQLTrait());
		}
	}
	/**
	 * 获取连接，优先选择数据源连接
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection() {
		Connection dbConnection;
		try {
			dbConnection = dataSourceProvider != null ? 
					dataSourceProvider.getDataSource().getConnection() : 
						DATA_SOURCE_NAME != null ? getDataSourceConnection() : getJDBCConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dbConnection;
	}			
	/**
	 * 获取数据源连接
	 * @return
	 * @throws Exception
	 */
	public static Connection getDataSourceConnection() {
		try {
			return ((DataSource) new InitialContext().lookup(DATA_SOURCE_NAME)).getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 获取jdbc连接
	 * @return
	 * @throws Exception
	 */
	public static Connection getJDBCConnection() throws Exception {
		return getJDBCConnection(DRIVER, URL, USER_NAME, PASSWORD);
	}
	public static Connection getJDBCConnection(String driver, String url, String userName, String password) throws Exception {
		Class.forName(driver);
		return DriverManager.getConnection(url, 
				userName, 
				password);
	}
}
