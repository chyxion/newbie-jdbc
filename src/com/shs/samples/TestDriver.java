package com.shs.samples;

import org.json.JSONArray;

import com.shs.framework.dao.BaseDAO;
import com.shs.framework.dao.DbManager;
import com.shs.framework.dao.BaseDAO.ConnectionOperator;
import com.shs.framework.dao.BaseDAO.ResultSetOperator;

public class TestDriver {
	public static void main(String[] args) {
		DbManager.DIALECT = "oralce";
		DbManager.DRIVER = "oracle.jdbc.OracleDriver";
		DbManager.URL = "jdbc:oracle:thin:@chyxion-pad:1521:oracle";
		DbManager.USER_NAME = "chyxion";
		DbManager.PASSWORD = "0211";
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
