package org.alefzero.padl.sources.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
		System.out.println(this.getConfig());

		logger.debug("Preparing sync for {} with id {}.", this.getConfig().getType(), this.getConfig().getId());

		params = (DBSourceParameters) this.getSourceParameters();
		config = (DBSourceConfiguration) this.getConfig();

		proxyHelper = new DBSourceServiceProxyHelper(params, config);
		dataHelper = new DBSourceServiceDataHelper(config);

		proxyHelper.cleanDatabases();
		proxyHelper.createOpenldapTables();
		proxyHelper.createDNSuffixTable();
		proxyHelper.createTables(dataHelper.getTableDefinitions());
		proxyHelper.loadOpenldapMappings();

	}

	public static void main(String[] args) {
		DBSourceParameters params;
		DBSourceConfiguration config;

		params = new DBSourceParameters();
		params.setDbDatabase("sql");
		params.setDbUsername("dbuser");
		params.setDbPassword("userpass");
		params.setDbServer("dev.local");
		params.setDbPort(3306);

		config = new DBSourceConfiguration();
		config.setInstanceId("instance1");
		config.setId("source1");
		config.setType("sql");
		config.setQuery("select * from users");
		// config.setQuery("select uid, email, name, surname, phone from users");
		config.setSourceJdbcURL("jdbc:mariadb://dev.local:3306/source");
		config.setSourceUsername("dbuser");
		config.setSourcePassword("userpass");
		config.setObjectClasses(Arrays.asList(new String[] { "inetOrgPerson" }));
		config.setSuffix("ou=users,dc=example,dc=org");
		
		List<DBSourceConfiguration.JoinData> list = new LinkedList<DBSourceConfiguration.JoinData>();

		DBSourceConfiguration.JoinData join = new DBSourceConfiguration.JoinData();
		join.setId("groups");
		join.setQuery("select * from groups");
		list.add(join);

		config.setJoinData(list);

		DBSourceService test = new DBSourceService();
		test.setConfig(config);
		test.setSourceParameters(params);
		test.prepare();
		System.out.println("Done");
	}
}
