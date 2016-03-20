package me.chyxion.jdbc;

import org.slf4j.Logger;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.LoggerFactory;

import me.chyxion.jdbc.pagination.MySQLCompatiblePaginationProcessor;
import me.chyxion.jdbc.pagination.OracleCompatibleProcessor;
import me.chyxion.jdbc.pagination.PaginationProcessor;

import java.sql.PreparedStatement;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion
 */
class DefaultDatabaseTraitResolver implements DatabaseTraitResolver {
	private static final Logger log = 
		LoggerFactory.getLogger(DefaultDatabaseTraitResolver.class);
	private PaginationProcessor mySQLCompatiblePaginationProcessor = 
		new MySQLCompatiblePaginationProcessor();
	private PaginationProcessor oracleCompatibleProcessor = 
		new OracleCompatibleProcessor();

	/**
	 * {@inheritDoc}
	 */
	public PaginationProcessor getPaginationProcessor(Connection conn) {
		String jdbcUrl = null;
		try {
			jdbcUrl = conn.getMetaData().getURL();
		}
		catch (SQLException e) {
			throw new IllegalStateException("Get Connection URL Error Caused", e);
		}
		log.debug("Create Pagination Processor Of JDBC URL [{}].", jdbcUrl);
        if (jdbcUrl.startsWith("jdbc:mysql:") || 
        	jdbcUrl.startsWith("jdbc:mariadb:") ||
        	jdbcUrl.startsWith("jdbc:postgresql:") ||
        	jdbcUrl.startsWith("jdbc:sqlite:") ||
        	jdbcUrl.startsWith("jdbc:cobar:") ||
        	jdbcUrl.startsWith("jdbc:h2:") || 
        	jdbcUrl.startsWith("jdbc:hsqldb:")) {
        	log.debug("Returns MySQLCompatiblePaginationProcessor.");
            return mySQLCompatiblePaginationProcessor;
        } 
        else if (jdbcUrl.startsWith("jdbc:oracle:") || 
        	jdbcUrl.startsWith("jdbc:alibaba:oracle:") ||
        	jdbcUrl.startsWith("jdbc:db2:") ||
        	jdbcUrl.startsWith("jdbc:sqlserver:")) {
        	log.debug("Returns OracleCompatibleProcessor.");
            return oracleCompatibleProcessor;
        } 
        else {
        	throw new IllegalStateException(
        		"Unsupported Database [" + jdbcUrl + 
        		"] Pagination, Please Set Pagination Provider");
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParam(PreparedStatement ps, 
			int index, Object param) throws SQLException {
		log.debug("Set Prepared Statement [{}] Index [{}] Param [{}].", ps, index, param);
		// set null
		if (param == null) { 
            int colType = Types.VARCHAR;
            try {
            	colType = ps.getParameterMetaData().getParameterType(index);
            } 
            catch (SQLException e) {
            	// ignore
            	log.debug("Get Sql Param Type [{}] Error Caused.", index, e);
            }
	        log.debug("Prepared Statement Set Index [{}] Null.", index);
			ps.setNull(index, colType);
		} 
		else {
			if (log.isDebugEnabled()) {
				log.debug("Prepared Statement Set Value [{}]#[{}]#[{}].", 
					index, param.getClass().getName(), param);
			}
			ps.setObject(index, param);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readValue(ResultSet rs, int index) throws SQLException {
		return rs.getObject(index);
	}
}
