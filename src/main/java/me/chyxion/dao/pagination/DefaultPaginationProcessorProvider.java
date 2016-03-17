package me.chyxion.dao.pagination;

import org.slf4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion
 */
public class DefaultPaginationProcessorProvider 
	implements PaginationProcessorProvider {
	private static final Logger log = 
		LoggerFactory.getLogger(DefaultPaginationProcessorProvider.class);

	/**
	 * {@inheritDoc}
	 */
	public PaginationProcessor create(Connection conn) {
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
            return new MySQLCompatiblePaginationProcessor();
        } 
        else if (jdbcUrl.startsWith("jdbc:oracle:") || 
        	jdbcUrl.startsWith("jdbc:alibaba:oracle:") ||
        	jdbcUrl.startsWith("jdbc:db2:") ||
        	jdbcUrl.startsWith("jdbc:sqlserver:")) {
        	log.debug("Returns OracleCompatibleProcessor.");
            return new OracleCompatibleProcessor();
        } 
        else {
        	throw new IllegalStateException(
        		"Unsupported Database [" + jdbcUrl + 
        		"] Pagination, Please Set Pagination Provider");
        }
	}
}
