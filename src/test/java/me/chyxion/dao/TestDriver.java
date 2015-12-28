package me.chyxion.dao;

import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory;

import me.chyxion.dao.Ro;
import me.chyxion.dao.Co;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.chyxion.dao.BaseDAO;
import me.chyxion.dao.BaseDAOSupport;

public class TestDriver {

	@Test
	public void test() throws Exception {
		DruidDataSource dds = new DruidDataSource();
		// dds.setDriver(driver);
		// ds.getConnection(username, password);
		dds.setUrl("jdbc:mysql://127.0.0.1/demo");
		dds.setUsername("root");
		dds.setPassword("0211");
		dds.init();
		BaseDAO dao = new BaseDAOSupport(dds);

		// dao.ee
		// dao.setLowerCase(true);
		dao.execute(new Co<Object>() {
			@Override
			protected Object run() {
				return null;
			}
		});

		dao.query(new Ro<String>() {
			public String exec(ResultSet rs) throws SQLException {
				while (rs.next()) {
					// jaResult.put(resultSet.getString(1));
				}
				return null;
				// result = jaResult;
			}
		}, "select name from demo_user");
	}
}
