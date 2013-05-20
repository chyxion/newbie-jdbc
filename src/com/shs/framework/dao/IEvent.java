package com.shs.framework.dao;

import java.sql.Connection;

public interface IEvent {
	void beforeQuery(Connection dbConnection);
	void afterQuery(Connection dbConnection);
	void beforeUpdate(Connection dbConnection);
	void afterUpdate(Connection dbConnection);
	void beforeInsert(Connection dbConnection);
	void afterInsert(Connection dbConnection);
	void beforeExecute(Connection dbConnection);
	void afterExecute(Connection dbConnection);
	void beforeExecuteBatch(Connection dbConnection);
	void afterExecuteBatch(Connection dbConnection);
}
