package org.alefzero.padl.sources.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alefzero.padl.exceptions.PadlUnrecoverableError;
import org.alefzero.padl.sources.impl.DBSourceConfiguration.JoinData;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.QuoteStrategies;

public class DBSourceServiceProxyHelper {
	protected static final Logger logger = LogManager.getLogger();

	private static final Integer SUFFIX_OBJECT_CLASS_ID = 1;

	private static final int PROXY_OBJECT_CLASS_ID = 2;

	private static final String SUFFIXES_TABLE = "suffixes";

	private static final String EXTRA_CLASSES_TABLE = "extra_classes";

	private static final Integer EXPIRATION_TIME_IN_MINUTES = 60;

	private int batchSize = 100;

	private BasicDataSource adminBds = null;
	private BasicDataSource bds = null;
	private DBSourceParameters params;
	private DBSourceConfiguration config;

	private Map<Integer, String> objectClasses = new HashMap<Integer, String>();

	private List<String> temporaryTables = new LinkedList<String>();

	public DBSourceServiceProxyHelper(DBSourceParameters params, DBSourceConfiguration config) {
		this.params = params;
		this.config = config;

		this.batchSize = config.getDbBatchSize();

		if (adminBds == null) {
			adminBds = new BasicDataSource();
			adminBds.setUrl(String.format("jdbc:mariadb://%s:%d/%s", params.getDbServer(), params.getDbPort(),
					"information_schema"));
			adminBds.setUsername(this.params.getDbUsername());
			adminBds.setPassword(this.params.getDbPassword());
			adminBds.setMaxTotal(2);
			adminBds.setMinIdle(1);
			adminBds.setCacheState(false);
		}

		if (bds == null) {
			bds = new BasicDataSource();
			bds.setUrl(String.format("jdbc:mariadb://%s:%d/%s", params.getDbServer(), params.getDbPort(),
					config.getDatabaseFullName()));
			bds.setUsername(params.getDbUsername());
			bds.setPassword(params.getDbPassword());
			bds.setMaxTotal(10);
			bds.setMinIdle(2);
			bds.setCacheState(false);
		}
		temporaryTables = getTempTables();
	}

	private List<String> getTempTables() {
		List<String> result = new LinkedList<String>();
		result.add(getTempTableName(config.getMetaTableName()));
		if (config.getJoinData() != null) {
			config.getJoinData().forEach(item -> result.add(getTempTableName(item.getMetaTableName())));
		}
		return result;

	}

	public boolean createTables(Map<String, DBSourceMeta> tableDefinition) {
		boolean result = true;
		try (Connection conn = bds.getConnection()) {
			List<String> sqls = new LinkedList<String>();
			for (String tableName : tableDefinition.keySet()) {
				sqls.addAll(getMetadataTableDefinitionsFor(tableName, tableDefinition.get(tableName), true));
				sqls.addAll(
						getMetadataTableDefinitionsFor(getTempTableName(tableName), tableDefinition.get(tableName),
								false));

			}
			sqls.forEach(sql -> {
				try {
					logger.debug("Creating auxiliary sql structure with definition: {}", sql);
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.executeUpdate();
					ps.close();
				} catch (SQLException e) {
					logger.error("Error running sql: {}", sql, e);
					e.printStackTrace();
				}
			});

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return result;
	}

	private List<String> getMetadataTableDefinitionsFor(String tableName, DBSourceMeta resultSetMetaData, boolean hasId)
			throws SQLException {
		List<String> definitions = new LinkedList<String>();

		String sql = "create table if not exists %s ( %s %s )";
		String colPadlId = hasId ? "padl_source_id serial, " : "";

		List<String> cols = new LinkedList<String>();

		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String precision;
			int sourcePrecision = resultSetMetaData.getPrecision(i);
			if (resultSetMetaData.getColumnType(i) == Types.DECIMAL
					|| resultSetMetaData.getColumnType(i) == Types.NUMERIC) {
				precision = String.format("(%d,%d)", sourcePrecision, resultSetMetaData.getScale(i));
			} else {
				precision = String.format("(%d)", sourcePrecision);
			}
			cols.add(String.format("%s %s %s", resultSetMetaData.getColumnName(i),
					getMariaDBTypeName(resultSetMetaData.getColumnType(i)), precision));
		}

		String createTable = String.format(sql, tableName, colPadlId,
				String.join(",", cols));

		String indexForOriginalSourceKey = String.format("create or replace index ndx_%s_%s on %s(%s)",
				tableName, resultSetMetaData.getIdColumn(), tableName, resultSetMetaData.getIdColumn());

		definitions.add(createTable);
		definitions.add(indexForOriginalSourceKey);

		if (hasId) {
			String indexForIds = String.format("create or replace index padl_ndx_%s on %s(padl_source_id)",
					tableName, tableName);
			definitions.add(indexForIds);
		}

		logger.debug("Returning create and index SQLs: {}", definitions.toArray());

		return definitions;
	}

	private String getMariaDBTypeName(int columnType) {
		String type;
		switch (columnType) {
			case Types.NUMERIC:
			case Types.DECIMAL:
				type = "DECIMAL";
				break;
			case Types.VARCHAR:
			case Types.NVARCHAR:
			default:
				type = "VARCHAR";
		}
		return type;
	}

	public void createOpenldapTables() {
		logger.debug("Creating metadata tables");

		this.sqlUpdate("""
				create table if not exists ldap_oc_mappings
				(
					id integer unsigned not null primary key auto_increment,
					name varchar(64) not null,
					keytbl varchar(64) not null,
					keycol varchar(64) not null,
					create_proc varchar(255),
					delete_proc varchar(255),
					expect_return tinyint not null
				)
				""");
		this.sqlUpdate("""
				create table if not exists ldap_attr_mappings
				(
					id integer unsigned not null primary key auto_increment,
					oc_map_id integer unsigned not null references ldap_oc_mappings(id),
					name varchar(255) not null,
					sel_expr varchar(255) not null,
					sel_expr_u varchar(255),
					from_tbls varchar(255) not null,
					join_where varchar(255),
					add_proc varchar(255),
					delete_proc varchar(255),
					param_order tinyint not null,
					expect_return tinyint not null
				)

				""");

		this.sqlUpdate("""
				create table if not exists ldap_entries
				(
					id integer unsigned not null primary key auto_increment,
					dn varchar(255) not null,
					oc_map_id integer unsigned not null references ldap_oc_mappings(id),
					parent int NOT NULL ,
					keyval int NOT NULL
				)
				""");

		this.sqlUpdate("""
				alter table ldap_entries add
				constraint unq1_ldap_entries unique if not exists
				(
					oc_map_id,
					keyval
				)
				""");

		this.sqlUpdate("""
				alter table ldap_entries add
				constraint unq2_ldap_entries unique if not exists
				(
					dn
				)
				""");

		// this.sqlUpdate("""
		// create table if not exists ldap_entry_objclasses
		// (
		// entry_id integer unsigned not null references ldap_entries(id),
		// oc_name varchar(64)
		// )
		// """);

		this.sqlUpdate(String.format("""
					create view if not exists ldap_entry_objclasses as select a.id as entry_id, b.classname as oc_name
					from ldap_entries a, extra_classes b where a.oc_map_id = %d;
				""", PROXY_OBJECT_CLASS_ID));

	}

	public void createPadlSupportTables() {
		this.sqlUpdate(String.format("""
				create table if not exists %s (suffix_id serial, suffix varchar(100))
				""", SUFFIXES_TABLE));

		this.sqlUpdate(String.format("""
				create table if not exists %s (classname varchar(50))
				""", EXTRA_CLASSES_TABLE));

	}

	public void loadOpenldapMappings() {

		objectClasses.put(SUFFIX_OBJECT_CLASS_ID, "organizationalUnit");
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("""
					insert into ldap_oc_mappings (id,name,keytbl,keycol,create_proc,delete_proc,expect_return)
					values (?,?,?,?,?,?,?)
					""");

			int i = 1;
			ps.setInt(i++, SUFFIX_OBJECT_CLASS_ID);
			ps.setString(i++, "organizationalUnit");
			ps.setString(i++, SUFFIXES_TABLE);
			ps.setString(i++, "suffix_id");
			ps.setNull(i++, Types.VARCHAR);
			ps.setNull(i++, Types.VARCHAR);
			ps.setInt(i++, 0);
			ps.executeUpdate();

			i = 1;
			ps.setInt(i++, PROXY_OBJECT_CLASS_ID);
			ps.setString(i++, config.getMainObjectClass());
			ps.setString(i++, config.getMetaTableName());
			ps.setString(i++, "padl_source_id");
			ps.setNull(i++, Types.VARCHAR);
			ps.setNull(i++, Types.VARCHAR);
			ps.setInt(i++, 0);
			ps.executeUpdate();
			ps.close();

			ps = conn.prepareStatement("insert into " + SUFFIXES_TABLE + "(suffix) values (?)");
			ps.setString(1, config.getSuffixName());
			ps.executeUpdate();
			ps.close();

			ps = conn.prepareStatement("insert into " + EXTRA_CLASSES_TABLE + " (classname) values (?)");
			for (String extraClass : config.getExtraClasses()) {
				ps.setString(1, extraClass);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	private void sqlUpdate(String sql) {
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void loadAttributes() {
		String sql = """
				insert into ldap_attr_mappings (oc_map_id,name,sel_expr,from_tbls,join_where,add_proc,delete_proc,param_order,expect_return)
				values (?,?,?,?,?,NULL,NULL,?,?);
				""";
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 1;
			ps.setInt(i++, SUFFIX_OBJECT_CLASS_ID);
			ps.setString(i++, config.getSuffixType());
			ps.setString(i++, "suffix");
			ps.setString(i++, SUFFIXES_TABLE);
			ps.setNull(i++, Types.VARCHAR);
			ps.setInt(i++, 0);
			ps.setInt(i++, 0);
			ps.executeUpdate();

			for (String ldapAttrib : config.getLdaptodb().keySet()) {
				i = 1;
				ps.setInt(i++, PROXY_OBJECT_CLASS_ID);
				ps.setString(i++, ldapAttrib);
				ps.setString(i++, config.getLdaptodb().get(ldapAttrib));
				ps.setString(i++, config.getMetaTableName());
				ps.setNull(i++, Types.VARCHAR);
				ps.setInt(i++, 0);
				ps.setInt(i++, 0);
				ps.executeUpdate();
			}

			for (JoinData join : config.getJoinData()) {
				for (String ldapAttrib : join.getLdaptodb().keySet()) {
					i = 1;
					ps.setInt(i++, PROXY_OBJECT_CLASS_ID);
					ps.setString(i++, ldapAttrib);
					ps.setString(i++,
							String.format("%s.%s", join.getMetaTableName(), join.getLdaptodb().get(ldapAttrib)));
					ps.setString(i++, String.format("%s,%s", config.getMetaTableName(), join.getMetaTableName()));
					ps.setString(i++, String.format("%s.%s=%s.%s", config.getMetaTableName(),
							join.getJoinColumnFromSource(), join.getMetaTableName(), join.getJoinColumnFromJoin()));
					ps.setInt(i++, 0);
					ps.setInt(i++, 0);
					ps.executeUpdate();
				}

			}
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	public void cleanTempTables() {
		logger.trace(".cleanTempTables()");
		logger.debug("Cleaning temporary tables.");
		try (Connection conn = bds.getConnection()) {
			for (String table : temporaryTables) {
				// sqlUpdate(String.format("delete from %s", table));
				sqlUpdate(String.format("truncate table %s", table));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public static class TableDataHelper {
		private String tableName;
		private String query;
		private List<String> columns;
		private String idColumn;
		private Boolean testLoadMode;

		public TableDataHelper(String tableName, String uidCol, String query, List<String> columns,
				Boolean toBeLoaded) {
			super();
			this.tableName = tableName;
			this.idColumn = uidCol;
			this.query = query;
			this.columns = columns;
			this.testLoadMode = toBeLoaded;
		}

		public Boolean getTestLoadMode() {
			return testLoadMode;
		}

		public void setTestLoadMode(Boolean toBeLoaded) {
			this.testLoadMode = toBeLoaded;
		}

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public List<String> getColumns() {
			return columns;
		}

		public void setColumns(List<String> columns) {
			this.columns = columns;
		}

		public String getIdColumn() {
			return idColumn;
		}

		public void setIdColumn(String uidCol) {
			this.idColumn = uidCol;
		}

	}

	public void loadData(DBSourceServiceDataHelper helper) throws SQLException {
		loadData(helper, true);
	}

	public void loadData(DBSourceServiceDataHelper helper, boolean batchLoad) throws SQLException {

		logger.debug("Loading data from source database.");

		List<TableDataHelper> items = getTableDataHelper();

		for (TableDataHelper item : items) {

			String template = "insert into %s ( %s )  values ( %s )";

			String colValues = "?,".repeat(item.getColumns().size());

			colValues = colValues.substring(0, colValues.length() - 1);

			String tempTableName = getTempTableName(item.getTableName());

			String sqlInsert = String.format(template, tempTableName,
					String.join(",", item.getColumns()), colValues);

			logger.debug("Loading data with: {}, batchmode: {} , batchSize: {}", sqlInsert, batchLoad, batchSize);

			try (Connection conn = bds.getConnection()) {
				logger.debug("Running: {}", String.format("alter table %s disable keys", tempTableName));

				conn.prepareStatement(String.format("alter table %s disable keys", tempTableName))
						.executeUpdate();

				PreparedStatement psLoad = conn.prepareStatement(sqlInsert);

				ResultSet sourceRs = helper.getDataFor(item.getQuery());

				if (batchLoad) {
					String lastId = "";
					try {
						int count = 0;
						while (sourceRs.next()) {
							lastId = sourceRs.getString(item.getIdColumn());
							for (int i = 0; i < item.getColumns().size(); i++) {
								String col = item.getColumns().get(i);
								psLoad.setObject(i + 1, sourceRs.getObject(col));
							}
							psLoad.addBatch();
							count++;
							if ((!item.getTestLoadMode()) && count > batchSize) {
								psLoad.executeBatch();
								count = 0;
							}
						}
						if ((!item.getTestLoadMode()) && count != 0) {
							psLoad.executeBatch();
						}
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error("Error processing batch for {}: {}", lastId, e);
					}

				} else {
					while (sourceRs.next()) {
						for (int i = 0; i < item.getColumns().size(); i++) {
							String col = item.getColumns().get(i);
							psLoad.setObject(i + 1, sourceRs.getObject(col));
						}
						if ((!item.getTestLoadMode())) {
							psLoad.executeUpdate();
						}
					}
				}
				psLoad.close();
				helper.closeResources(sourceRs);
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
			try (Connection conn = bds.getConnection()) {
				logger.debug("Running: {}", String.format("alter table %s enable keys", tempTableName));
				conn.prepareStatement(String.format("alter table %s enable keys", tempTableName))
						.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}

	}

	private List<TableDataHelper> getTableDataHelper() {

		LinkedList<TableDataHelper> tableDatum = new LinkedList<TableDataHelper>();

		TableDataHelper tableData = new TableDataHelper(config.getMetaTableName(), config.getIdColumn(),
				config.getQuery(), new LinkedList<String>(config.getDbtoldap().keySet()), config.getTestLoadMode());

		if (!tableData.getColumns().contains(config.getIdColumn())) {
			tableData.getColumns().add(config.getIdColumn());
		}

		tableDatum.add(tableData);

		config.getJoinData().forEach(item -> {
			List<String> list = new LinkedList<String>(item.getDbtoldap().keySet());
			if (!list.contains(config.getIdColumn())) {
				list.add(config.getIdColumn());
			}
			tableDatum.add(new TableDataHelper(item.getMetaTableName(), item.getIdColumn(), item.getQuery(), list,
					config.getTestLoadMode()));

		});

		return tableDatum;
	}

	private String getTempTableName(String metaTableName) {
		return metaTableName + "_temp";
	}

	public void mergeData() {
		logger.debug("Merging data from source into ldap tables.");

		List<TableDataHelper> tableDatum = getTableDataHelper();

		for (TableDataHelper tableData : tableDatum) {
			try (Connection conn = bds.getConnection()) {
				String proxyTableName = tableData.getTableName();
				String idColumn = tableData.getIdColumn();
				String tempTableName = getTempTableName(proxyTableName);

				// List<String> fmtJoinAllCols = tableData.getColumns().stream()
				// .map(col -> String.format("proxytable.%s = temptable.%s", col, col))
				// .collect(Collectors.toList());

				// String allJoinCols = String.join(" and ", fmtJoinAllCols);

				String allCols = String.join(",", tableData.getColumns());

				// String sqlDeleteDiff = String.format("""
				// delete from %s where padl_source_id in (
				// select proxytable.padl_source_id
				// from %s as proxytable
				// left join %s as temptable
				// on
				// %s
				// where temptable.%s is null
				// )
				// """, proxyTableName, proxyTableName, tempTableName, allJoinCols, idColumn);

				String sqlDeleteDiff = String.format("""
						delete from %s where padl_source_id in (
						select proxytable.padl_source_id
						from
						   %s as proxytable
						   inner join (
						      select %s from %s
							  except
							  select %s from %s
						   ) as temptable
						   on proxytable.%s = temptable.%s
						)
						""", proxyTableName, proxyTableName, allCols, proxyTableName, allCols, tempTableName, idColumn,
						idColumn);

				logger.debug("Runing sync delete phase with: \n" + sqlDeleteDiff);
				PreparedStatement psDelete = conn.prepareStatement(sqlDeleteDiff);
				psDelete.executeUpdate();
				psDelete.close();

				String sqlInsertDiff = String.format("""
						insert into %s ( %s )
						select %s from %s as temptable
						except
						select %s from %s as proxytable
							""", proxyTableName, allCols, allCols, tempTableName, allCols, proxyTableName);

				logger.debug("Runing sync insert phase with: \n" + sqlInsertDiff);

				PreparedStatement psInsert = conn.prepareStatement(sqlInsertDiff);

				psInsert.executeUpdate();
				psInsert.close();

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}

	public void updateEntries() {
		logger.debug("Adding entries to ldap instance.");

		try (Connection conn = bds.getConnection()) {

			String suffixDN = config.getSuffix();

			String sqlUpdateSuffix = String.format("""
					insert into ldap_entries (dn,oc_map_id,parent,keyval)
					select '%s', %d, 0 as parent, suffix_id
					from suffixes
					where suffix_id not in
					(select keyval from ldap_entries where oc_map_id = ?)
					""", suffixDN, SUFFIX_OBJECT_CLASS_ID);

			logger.debug("Checking suffix with:\n" + sqlUpdateSuffix);

			PreparedStatement psUpdateSuffix = conn.prepareStatement(sqlUpdateSuffix);
			psUpdateSuffix.setInt(1, SUFFIX_OBJECT_CLASS_ID);
			psUpdateSuffix.executeUpdate();
			psUpdateSuffix.close();

			String sqlDeleteEntries = String.format("""
					delete from ldap_entries
					where
						oc_map_id = ?
						and keyval not in (select padl_source_id from %s)
					""", config.getMetaTableName());

			logger.debug("Removing entries with:\n" + sqlDeleteEntries);

			PreparedStatement psDeleteEntries = conn.prepareStatement(sqlDeleteEntries);
			psDeleteEntries.setInt(1, PROXY_OBJECT_CLASS_ID);
			psDeleteEntries.executeUpdate();
			psDeleteEntries.close();

			PreparedStatement psGetSuffixId = conn
					.prepareStatement("select suffix_id from %s where suffix = ?".formatted(SUFFIXES_TABLE));
			psGetSuffixId.setString(1, config.getSuffixName());
			ResultSet rsSuffix = psGetSuffixId.executeQuery();

			int suffixId = 0;

			if (rsSuffix.next()) {
				suffixId = rsSuffix.getInt(1);

			} else {
				throw new PadlUnrecoverableError("Suffix id cannot be null");
			}

			String dnClause = "concat('%s ,',' , '%s')"
					.formatted(config.getDnFormat().formatted("'," + config.getIdColumn()), config.getSuffix());

			String sqlInsertEntries = String.format("""
					insert into ldap_entries (dn,oc_map_id,parent,keyval)
					select %s, %d as oc_map_id, %d as parent, padl_source_id
					from %s
					where padl_source_id not in
					(select keyval from ldap_entries where oc_map_id = ?)
					""", dnClause, PROXY_OBJECT_CLASS_ID, suffixId, config.getMetaTableName());

			logger.debug("Inserting new entries with:\n" + sqlInsertEntries);

			PreparedStatement psInsertEntries = conn.prepareStatement(sqlInsertEntries);
			psInsertEntries.setInt(1, PROXY_OBJECT_CLASS_ID);
			psInsertEntries.executeUpdate();
			psInsertEntries.close();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void createMetaData() {
		logger.debug("Running cleaning database process with base name [basename='{}",
				config.getDatabaseBaseName() + "%']");

		String sqlCreateMetaSchema = "create database if not exists padl_meta";
		String sqlCreateMetaTable = """
				create table if not exists padl_meta.instances (instance_basename varchar(30), instance_dbname varchar(50),
				 last_ping timestamp, status varchar(10), config_version varchar(100),
				 primary key (instance_basename, instance_dbname))
				 """;
		String sqlGrantLoadCSV = String.format("grant file on *.* to '%s'@'%%'", params.getDbUsername());
		try (Connection conn = adminBds.getConnection()) {
			conn.prepareStatement(sqlCreateMetaSchema).executeQuery();
			conn.prepareStatement(sqlCreateMetaTable).executeQuery();
			logger.debug("Granting infile privileges: {}", sqlGrantLoadCSV);
			conn.prepareStatement(sqlGrantLoadCSV).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void cleanDatabases() {
		logger.debug("Running cleaning database process with base name [basename='{}",
				config.getDatabaseBaseName() + "%']");
		String sqlGetExpiredInstancesDBNames = String.format(
				"select instance_dbname from padl_meta.instances where instance_basename = ? and last_ping < now() - interval %d minute",
				EXPIRATION_TIME_IN_MINUTES);

		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement psExpired = conn.prepareStatement(sqlGetExpiredInstancesDBNames);
			psExpired.setString(1, config.getDatabaseBaseName());
			ResultSet rsExpired = psExpired.executeQuery();
			while (rsExpired.next()) {
				conn.prepareStatement("drop database if exists " + rsExpired.getString(1)).executeUpdate();
			}

			// old note to query information_schema "select schema_name from
			// information_schema.schemata where schema_name = ?"
			PreparedStatement psCreateSchemaIfNeeded = conn
					.prepareStatement("create database if not exists " + config.getDatabaseFullName());
			psCreateSchemaIfNeeded.executeUpdate();
			psCreateSchemaIfNeeded.close();

			pingMetaSchema(config.getDatabaseBaseName(), config.getDatabaseFullName());

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	private void pingMetaSchema(String databaseBaseName, String databaseFullName) {
		String sqlUpdatePing = "update padl_meta.instances set last_ping = now() where instance_dbname = ?";
		String sqlInsertPing = "insert into padl_meta.instances ( instance_basename, instance_dbname, last_ping, status, config_version) values (?,?,now(),?,?)";

		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement psUpdatePing = conn.prepareStatement(sqlUpdatePing);
			psUpdatePing.setString(1, databaseFullName);
			if (psUpdatePing.executeUpdate() == 0) {
				PreparedStatement psInsertPing = conn.prepareStatement(sqlInsertPing);
				psInsertPing.setString(1, databaseBaseName);
				psInsertPing.setString(2, databaseFullName);
				psInsertPing.setString(3, MetaDbStatus.NEW.toString());
				psInsertPing.setString(4, "");
				psInsertPing.executeUpdate();
				psInsertPing.close();
			}
			psUpdatePing.close();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public MetaDbStatus getDbCurrentStatus(String databaseFullName) {
		String sqlGetCurrentStatus = "select status from padl_meta.instances where instance_dbname = ?";
		MetaDbStatus result = MetaDbStatus.NO_STATUS;
		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sqlGetCurrentStatus);
			ps.setString(1, databaseFullName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = MetaDbStatus.valueOf(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return result;
	}

	public void setDbCurrentStatus(String databaseFullName, MetaDbStatus configured) {
		String sqlGetCurrentStatus = "update padl_meta.instances set status = ? where instance_dbname = ?";
		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sqlGetCurrentStatus);
			ps.setString(1, configured.toString());
			ps.setString(2, databaseFullName);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void createAuxiliaryIndexes(DBSourceConfiguration config) {
		List<String> sqlIndexes = new LinkedList<String>();
		int indexCount = 1;
		if (config.getIndexCols().size() > 0) {
			String indexCols = String.join(", ", config.getIndexCols());
			sqlIndexes.add(String.format("create or replace index ndx_aux_%s_%s on %s (%s)", config.getMetaTableName(),
					indexCount,
					config.getMetaTableName(), indexCols));
		}

		for (JoinData joindata : config.getJoinData()) {
			if (joindata.getIndexCols().size() > 0) {
				String indexCols = String.join(", ", joindata.getIndexCols());
				sqlIndexes.add(String.format("create or replace index ndx_aux_%s_%s on %s (%s)",
						joindata.getMetaTableName(), indexCount,
						joindata.getMetaTableName(), indexCols));
			}
		}

		sqlIndexes.forEach(sql -> this.sqlUpdate(sql));

	}

	public void loadDataInfile(DBSourceServiceDataHelper helper) throws SQLException {
		logger.debug("Loading data from source database using infile mode.");
		for (TableDataHelper item : getTableDataHelper()) {
			// get all data in a file in csv format
			ResultSet sourceRs = helper.getDataFor(item.getQuery());
			try (Connection connFromDataSource = sourceRs.getStatement().getConnection()) {
				Path file = exportToCSV(sourceRs, item);
				importFromCSV(file, item);
			} catch (SQLException | IOException e) {
				e.printStackTrace();
				logger.error("Error processing data from table {}: {}", item.getTableName(), e.getLocalizedMessage());
			}
		}
	}

	private Path exportToCSV(ResultSet sourceRs, TableDataHelper item) throws SQLException, IOException {
		Path file = Files.createTempFile(item.getTableName(), ".tmp",
				PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")));
		CsvWriter writer = CsvWriter.builder()
				.quoteStrategy(QuoteStrategies.ALWAYS)
				.quoteCharacter('"')
				.fieldSeparator(';')
				.build(file, StandardCharsets.UTF_8);
		List<String> cols = item.getColumns();
		writer.writeRecord(cols);
		while (sourceRs.next()) {
			List<String> data = new LinkedList<String>();
			for (String col : cols) {
				data.add(sourceRs.getString(col));
			}
			writer.writeRecord(data);
		}
		writer.close();
		return file;
	}

	private void importFromCSV(Path file, TableDataHelper item) throws SQLException {
		List<String> metaCols = item.getColumns().stream()
				.map(colName -> "@" + colName)
				.collect(Collectors.toList());
		List<String> dataCols = item.getColumns().stream()
				.map(colName -> colName + " = @" + colName)
				.collect(Collectors.toList());

		String sqlLoadData = String.format("""
				load data infile %s into table %s
				fields terminated by ';' enclosed by '\"' ignore 1 lines
				(%s) set %s""", file.toAbsolutePath().toString(), getTempTableName(item.getTableName()),
				String.join(", ", metaCols), String.join(", ", dataCols));
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sqlLoadData);
			ps.executeQuery();
		} catch (SQLException e) {
			logger.error("Error loading into temporary table data:", e);
			logger.error("tempFile: {}, sqlLoadData: {} ", sqlLoadData);
			throw e;
		} finally {
			// TODO dont delete file when exception
		}
		file.toFile().delete();
	}

	public static void main(String[] args) throws Exception {
		String sqlGrantLoadCSV = String.format("grant file on *.* to '%s'@'%%'", "user");
		System.out.println(sqlGrantLoadCSV);
	}

}
