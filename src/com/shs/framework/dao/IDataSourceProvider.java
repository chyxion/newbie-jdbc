package com.shs.framework.dao;

import javax.sql.DataSource;

public interface IDataSourceProvider {
	public DataSource getDataSource();
}
