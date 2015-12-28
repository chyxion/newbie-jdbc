package me.chyxion.xls;

import java.sql.ResultSet;
import me.chyxion.dao.Co;
import me.chyxion.dao.BaseDAO;
import me.chyxion.dao.BaseDAOSupport;
import me.chyxion.dao.Ro;

public class TestDriver {
	public static void main(String[] args) {
		BaseDAO dao = new BaseDAOSupport();
		// dao.ee
		// dao.setLowerCase(true);
		dao.execute(new Co<Object>() {
			@Override
			protected Object run() {
				return null;
			}
		});

		dao.query(new Ro<String>() {
			public String run(ResultSet rs) throws Exception {
				while (rs.next()) {
					// jaResult.put(resultSet.getString(1));
				}
				return null;
				// result = jaResult;
			}
		}, "select name from demo_users");
	}
}
