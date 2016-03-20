package me.chyxion.jdbc;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.LinkedList;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.alibaba.druid.pool.DruidDataSource;

import me.chyxion.jdbc.Co;
import me.chyxion.jdbc.NewbieJdbc;
import me.chyxion.jdbc.NewbieJdbcSupport;
import me.chyxion.jdbc.Order;
import me.chyxion.jdbc.Ro;

public class TestDriver {
	DruidDataSource ds = null;
	private NewbieJdbc jdbc = null;
	{
		ds = new DruidDataSource();
		// dds.setDriver(driver);
		// ds.getConnection(username, password);
		ds.setUrl("jdbc:mysql://127.0.0.1/demo");
		ds.setUsername("root");
		ds.setPassword("0211");
		try {
			ds.init();
		}
		catch (SQLException e) {
			throw new IllegalStateException(
				"Init Data Source Error Caused", e);
		}
		jdbc = new NewbieJdbcSupport(ds);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void finalize() throws Throwable {
		ds.close();
	}

	private String id() {
		return UUID.randomUUID().toString();
	}

	@Test
	public void test() throws Exception {
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("id", id());
		user.put("name", "Shaun Chyxion");
		user.put("gender", "M");
		user.put("date_created", new Date());
		// dao.insert("demo_user", user);

		List<String> nn = jdbc.execute(new Co<List<String>>() {
			@Override
			protected List<String> run() throws SQLException {
				String url = conn.getMetaData().getURL();
				System.err.println(url);
				return null;
			}
		});

		List<String> names = jdbc.list(new Ro<String>() {
			public String exec(ResultSet rs) throws SQLException {
				return rs.getString("name");
			}
		}, "select name from demo_user");
		System.err.println(names);
		System.err.println(jdbc.findValue("select count(1) from demo_user"));
		System.err.println(jdbc.listMapPage("select * from demo_user", 
			Arrays.asList(new Order("name", Order.DESC)), 0, 1));
	}

	@Test
	public void testUpdate() throws Exception {
		jdbc.update("update demo_user set name = ? where id = ?", 
			"Name Updated", "ff1779f4-48d9-44d4-8b86-f280a4b33fe6");
	}

	@Test
	public void testTransaction() throws Exception {
		jdbc.executeTransaction(new Co<Integer>() {
			@Override
			protected Integer run() throws SQLException {
				update("update demo_user set name = ? where id = ?", 
					"Name Updated 3", "ff1779f4-48d9-44d4-8b86-f280a4b33fe6");
				throw new IllegalStateException();
			}
		});
	}

	@Test
	public void testExcuteBatch() throws Exception {
		List<List<Object>> args = new LinkedList<List<Object>>();
		for (int i = 0; i < 32; ++i) {
			args.add(Arrays.<Object>asList("id_" + i, 
				"Shaun Chyxion " + i, 
				i % 2 == 0 ? "M" : "F",
				new Date()));
		}

		jdbc.executeBatch(
			"insert into demo_user(id, name, gender, date_created) " + 
			"values (?, ?, ?, ?)", 3, args.toArray(new List<?>[0]));
	}

	@Test
	public void testFindIn() throws Exception {
		List<Object> names = jdbc.listValue(
			"select name from demo_user where gender in (?) and name like ?", 
			Arrays.asList("M", "F"), "%Shaun%");
		System.err.println(names);
	}
}
