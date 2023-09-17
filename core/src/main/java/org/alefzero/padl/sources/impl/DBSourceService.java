package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceService extends PadlSourceService {
	protected static final Logger logger = LogManager.getLogger();
	private DBSourceParameters params;
	private DBSourceConfiguration config;

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
		proxyHelper.createOpenldapTables();
		
		
		dataHelper.getTableDefinitions().forEach(tableDefinition -> proxyHelper.createTable(tableDefinition));


	}

}
