package org.alefzero.padl.sources.impl;

import java.util.Objects;

import org.alefzero.padl.sources.PadlSourceService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceService extends PadlSourceService {
	protected static final Logger logger = LogManager.getLogger();
	private BasicDataSource bds = null;
	private DBSourceParameters params;
	private DBSourceConfiguration config;
	private String jdbcCacheURL = "";

	private DBSourceServiceProxyHelper proxyHelper;
	private DBSourceServiceDataHelper dataHelper;

	public DBSourceService() {
		super();
	}

	@Override
	public void sync() {
		// TODO implement
	}

	@Override
	public void prepare() {
		logger.debug("Preparing sync for {} with id {}.", this.getConfig().getType(), this.getConfig().getId());

		proxyHelper = new DBSourceServiceProxyHelper(params, config);
		dataHelper = new DBSourceServiceDataHelper(config);

		proxyHelper.cleanDatabases();
		dataHelper.getTableDefinitions().forEach(tableDefinition -> proxyHelper.createTable(tableDefinition));

		proxyHelper.createOpenldapTables();

	}

	private void initializeResources() {
		if (params == null) {
			params = (DBSourceParameters) this.getSourceParameters();
			Objects.requireNonNull(params, "Could not set parameters for service.");
			config = (DBSourceConfiguration) this.getConfig();
			Objects.requireNonNull(params, "Could not set configuration for service.");
			jdbcCacheURL = String.format("jdbc:mariadb://%s:%d/%s", params.getDbServer(), params.getDbPort(),
					config.getDbDatabaseId());
			Objects.requireNonNull(jdbcCacheURL, "Could not set JDBC Cache URL for service.");
			if (bds == null) {
				bds = new BasicDataSource();
				bds.setUrl(jdbcCacheURL);
				bds.setUsername(params.getDbUsername());
				bds.setPassword(params.getDbPassword());
				bds.setMaxTotal(10);
				bds.setMinIdle(2);
				bds.setCacheState(false);
			}
			Objects.requireNonNull(bds, "Could not datasource for service.");
		}
	}

}
