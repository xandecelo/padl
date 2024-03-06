package org.alefzero.padl.sources.impl;

import java.sql.SQLException;
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
		logger.trace(".sync()");
		if ("inline".equalsIgnoreCase(config.getLoadMode())) {
			try {
				proxyHelper.cleanTempTables();
				proxyHelper.loadDataInline(dataHelper);
				proxyHelper.mergeData();
				proxyHelper.updateEntries();
				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}	

		} else {
			// loadMode defaults to... "default" (surprise!)
			try {
				proxyHelper.cleanTempTables();
				proxyHelper.loadData(dataHelper, config.getBatchMode());
				proxyHelper.mergeData();
				proxyHelper.updateEntries();
				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}	
		}
		logger.trace(".sync() endend.");
	}

	@Override
	public void prepare() {
		System.out.println(this.getConfig());

		logger.debug("Preparing sync for {} with id {}.", this.getConfig().getType(), this.getConfig().getId());

		params = (DBSourceParameters) this.getSourceParameters();
		config = (DBSourceConfiguration) this.getConfig();

		proxyHelper = new DBSourceServiceProxyHelper(params, config);
		dataHelper = new DBSourceServiceDataHelper(config);

		proxyHelper.createMetaData();
		proxyHelper.cleanDatabases();
		proxyHelper.createPadlSupportTables();
		proxyHelper.createOpenldapTables();
		proxyHelper.createTables(dataHelper.getTableDefinitions());
		if (proxyHelper.getDbCurrentStatus(config.getDatabaseFullName()) == MetaDbStatus.NEW) {
			proxyHelper.loadOpenldapMappings();
			proxyHelper.loadAttributes();
			proxyHelper.setDbCurrentStatus(config.getDatabaseFullName(), MetaDbStatus.RUNNING);
		}
		proxyHelper.createAuxiliaryIndexes(config);

	}

	public static void main1(String[] args) {
		DBSourceParameters params;
		DBSourceConfiguration config;

		params = new DBSourceParameters();
		params.setDbUsername("dbuser");
		params.setDbPassword("userpass");
		params.setDbServer("dev.local");
		params.setDbPort(3306);

		config = new DBSourceConfiguration();
		config.setInstanceId("instance1");
		config.setId("source1");
		config.setType("sql");
		config.setQuery("select * from users");
		config.setSourceJdbcURL("jdbc:mariadb://dev.local:3306/source");
		config.setSourceUsername("dbuser");
		config.setSourcePassword("userpass");
		config.setObjectClasses(Arrays.asList(new String[] { "inetOrgPerson" }));
		config.setSuffix("ou=users,dc=example,dc=org");
		config.setBaseSuffix("dc=example,dc=org");
		config.setIdColumn("uid");
		config.setAttributes(
				new String[] { "givenName=name", " sn=surname", "cn=uid", "telephoneNumber=phone", "mail=email" });
		List<DBSourceConfiguration.JoinData> list = new LinkedList<DBSourceConfiguration.JoinData>();

		DBSourceConfiguration.JoinData join = new DBSourceConfiguration.JoinData();
		join.setId("groups");
		join.setQuery("select groupname, username from groups a inner join users b on a.username = b.uid");
		join.setAttributes(new String[] { "memberOf=groupname" });
		join.setJoinColumns("uid=username");
		join.setIdColumn("username");

		list.add(join);

		config.setJoinData(list);

		DBSourceService test = new DBSourceService();
		test.setConfig(config);
		test.setSourceParameters(params);
		test.prepare();
		test.sync();
		System.out.println("Done");
	}
}
