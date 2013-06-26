package com.shs.samples;

import org.json.JSONArray;

import com.shs.framework.dao.BaseDAO;
import com.shs.framework.dao.ConnectionManager;
import com.shs.framework.dao.BaseDAO.ConnectionOperator;
import com.shs.framework.dao.BaseDAO.ResultSetOperator;

public class TestDriver {
	public static void main(String[] args) {
		ConnectionManager.DIALECT = "oralce";
		ConnectionManager.DRIVER = "oracle.jdbc.OracleDriver";
		ConnectionManager.URL = "jdbc:oracle:thin:@chyxion-pad:1521:oracle";
		ConnectionManager.USER_NAME = "chyxion";
		ConnectionManager.PASSWORD = "0211";
		BaseDAO dao = new BaseDAO();
		dao.setLowerCase(true);
		dao.execute(new ConnectionOperator() {
			@Override
			protected void run() throws Exception {
				
			}
		});
		dao.query(new ResultSetOperator() {
			@Override
			protected void run() throws Exception {
				JSONArray jaResult = new JSONArray();
				while (resultSet.next()) {
					jaResult.put(resultSet.getString(1));
				}
				result = jaResult;
			}
		}, "select name from demo_users");
	}
}
