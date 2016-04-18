package me.chyxion.jdbc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import me.chyxion.jdbc.Co;
import me.chyxion.jdbc.Ro;
import me.chyxion.jdbc.Order;
import me.chyxion.jdbc.NewbieJdbc;
import me.chyxion.jdbc.NewbieJdbcSupport;
import me.chyxion.jdbc.pagination.MySQLCompatiblePaginationProcessor;
import me.chyxion.jdbc.pagination.PaginationProcessor;
import com.alibaba.druid.pool.DruidDataSource;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion <br>
 * chyxion@163.com <br>
 * Mar 26, 2016 4:11:23 PM
 */
public class TestDriver {
	private static final Logger log = 
		LoggerFactory.getLogger(TestDriver.class);

	DruidDataSource dataSource = null;
	private NewbieJdbc jdbc = null;
	{
		dataSource = new DruidDataSource();
		dataSource.setUrl("jdbc:mysql://127.0.0.1/demo");
		dataSource.setUsername("root");
		dataSource.setPassword("0211");
		try {
			dataSource.init();
		}
		catch (SQLException e) {
			throw new IllegalStateException(
				"Init Data Source Error Caused", e);
		}
		jdbc = new NewbieJdbcSupport(dataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void finalize() throws Throwable {
		dataSource.close();
	}

	@Test
	public void test() throws Exception {
		Map<String, Object> mapUser = new HashMap<String, Object>();
		mapUser.put("id", "103");
		mapUser.put("name", "Shaun Chyxion");
		mapUser.put("gender", "M");
		mapUser.put("date_created", new Date());
		jdbc.insert("users", mapUser);

		Collection<Collection<?>> users = 
			    Arrays.<Collection<?>>asList(
			        Arrays.<Object>asList("104", "Xuir", "F", new Date()), 
			        Arrays.<Object>asList("105", "Sorina Nyco", "F", new Date()), 
			        Arrays.<Object>asList("106", "Gemily", "F", new Date()), 
			        Arrays.<Object>asList("107", "Luffy", "M", new Date()), 
			        Arrays.<Object>asList("108", "Zoro", "M", new Date()), 
			        Arrays.<Object>asList("109", "Bruck", "M", new Date()));
			jdbc.insert("users", 
			    Arrays.asList("id", "name", "gender", "date_created"), 
			    users, 
			    16);

			List<String> names = jdbc.list(new Ro<String>() {
				public String exec(ResultSet rs) throws SQLException {
					return rs.getString("name");
				}
			}, "select name from demo_user");
			log.info("List Names [{}].", names);
			jdbc.update("update user set gender = ? where id = ?", "F", "102");

			Map<String, Object> user = jdbc.executeTransaction(new Co<Map<String, Object>>() {
				@Override
				protected Map<String, Object> run() throws SQLException {
					update("delete users where id = ?", "104");
					update("update users set age = ? where id = ?", 24, "103");
					return findMap("select * from users where id = ?", 106);
				}
			});
			log.info("User [{}].", user);
			int count = jdbc.findValue("select count(1) from demo_user");
			log.info("Count [{}].", count);
			List<Map<String, Object>> usersPage = jdbc.listMapPage("select * from demo_user", 
				Arrays.asList(new Order("name", Order.DESC)), 0, 1);
			log.info("Users Page [{}].", usersPage);

			usersPage = jdbc.listMapPage("select * from users where gender = ?", 
				Arrays.asList(new Order("date_created", Order.DESC)), 10, 16, "F");
			log.info("Users Page [{}].", usersPage);
			count = jdbc.findValue(
				"select count(1) from users");

			String name = jdbc.findValue(
				"select name from users where id = ?", 
					"2008110101");
			log.info("Find Value [{}].", name);
			names = jdbc.listValue(
				"select name from users where id in (?)", 
				"2008110101", 
				"2008110102");
			names = jdbc.listValue(
				"select name from users where id in (?)", 
				Arrays.asList("2008110101", "2008110102"));

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", Arrays.asList("2008110101", "2008110102"));
			names = jdbc.listValue(
				"select name from users where id in (:id)", params);

			List<Map<String, Object>> listUsers = jdbc.listMap(
					"select id, name, gender from users where age = ?", 24);
			String name1 = jdbc.query(new Ro<String>() {
				public String exec(ResultSet rs) throws SQLException {
					return rs.next() ? rs.getString(1) : null;
				}
			}, 
			"select name from users where id = ?", 
			"101");

		String[] idAndName = jdbc.findOne(new Ro<String[]>() {
			public String[] exec(ResultSet rs) throws SQLException {
				return new String[] {rs.getString("id"), rs.getString("name")};
			}
		}, "select id, name from users where id = ?", "101");

		// 
		names = jdbc.list(new Ro<String>() {
			public String exec(ResultSet rs) throws SQLException {
				return rs.getString("name");
			}
		}, 
		"select name from users where gender = ?", 
		"M");
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
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("gender", Arrays.asList("M", "F"));
		mapParam.put("name", "%Shaun%");
		names = jdbc.listValue(
			"select name from demo_user where gender in (:gender) and name like :name", mapParam);
		System.err.println(names);
	}

	@Test
	public void batchInsert() {
		Collection<Collection<?>> users = 
			    Arrays.<Collection<?>>asList(
			        Arrays.<Object>asList("1004", "Xuir", "F", new Date()), 
			        Arrays.<Object>asList("1005", "Sorina Nyco", "F", new Date()), 
			        Arrays.<Object>asList("1006", "Gemily", "F", new Date()), 
			        Arrays.<Object>asList("1007", "Luffy", "M", new Date()), 
			        Arrays.<Object>asList("1008", "Zoro", "M", new Date()), 
			        Arrays.<Object>asList("1009", "Bruck", "M", new Date()));
			jdbc.insert("demo_user", 
			    Arrays.asList("id", "name", "gender", "date_created"), 
			    users, 
			    2);
	}
	
	@Test
	public void testCustom() {
		CustomResolver databaseTraitResolver = new CustomResolver() {
			
			public void setParam(PreparedStatement ps, int index, Object param)
					throws SQLException {
				if (param instanceof StringBuilder) {
					ps.setString(index, param.toString());
				}
				else {
					ps.setObject(index, param);
				}
			}
			
			public Object readValue(ResultSet rs, int index) throws SQLException {
				Object valueRtn = null;
				if (Types.CLOB == rs.getMetaData().getColumnType(index)) {
					valueRtn = rs.getClob(index).toString();
				}
				else {
					valueRtn = rs.getObject(index);
				}
				return valueRtn;
			}
			
			public PaginationProcessor getPaginationProcessor(Connection conn) {
				return new MySQLCompatiblePaginationProcessor();
			}
		};

		jdbc = new NewbieJdbcSupport(dataSource, databaseTraitResolver);

		jdbc.update("update demo_user set name = ? where id = ?", 
			"Name Updated", "ff1779f4-48d9-44d4-8b86-f280a4b33fe6");
	}
}
