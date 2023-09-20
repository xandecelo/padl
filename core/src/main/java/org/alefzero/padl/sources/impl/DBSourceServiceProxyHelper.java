package org.alefzero.padl.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.alefzero.padl.sources.impl.DBSourceConfiguration.JoinData;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceServiceProxyHelper {
	protected static final Logger logger = LogManager.getLogger();

	private static final Integer SUFFIX_OBJECT_CLASS_ID = 1;

	private static final int PROXY_OBJECT_CLASS_ID = 2;

	private static final String SUFFIXES_TABLE = "suffixes";

	private static final Object EXTRA_CLASSES_TABLE = "extra_classes";

	private BasicDataSource adminBds = null;
	private BasicDataSource bds = null;
	private DBSourceParameters params;
	private DBSourceConfiguration config;

	private static Integer randomId = new Random().nextInt(99_999);

	private Map<Integer, String> objectClasses = new HashMap<Integer, String>();

	private List<String> temporaryTables = new LinkedList<String>();

	public DBSourceServiceProxyHelper(DBSourceParameters params, DBSourceConfiguration config) {
		this.params = params;
		this.config = config;

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
					this.getDatabaseName()));
			bds.setUsername(params.getDbUsername());
			bds.setPassword(params.getDbPassword());
			bds.setMaxTotal(10);
			bds.setMinIdle(2);
			bds.setCacheState(false);
		}
	}

	private String getDatabaseName() {
		return String.format("%s_%05d", getDatabaseBaseName(), randomId);
	}

	private String getDatabaseBaseName() {
		return String.format("%s_%s", config.getInstanceId(), config.getId());
	}

	public void cleanDatabases() {
		logger.debug("Running cleaning database process with base name [basename='{}", getDatabaseBaseName() + "%']");
		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(
					"select schema_name from information_schema.schemata where schema_name like ? order by 1");
			ps.setString(1, getDatabaseBaseName() + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				logger.debug("Removing schema {}.", rs.getString(1));
				PreparedStatement psRemove = conn.prepareStatement("drop database " + rs.getString(1));
				psRemove.executeUpdate();
				psRemove.close();
			}
			rs.close();
			ps.close();
			PreparedStatement psCreate = conn.prepareStatement("create database " + this.getDatabaseName());
			psCreate.executeUpdate();
			psCreate.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public boolean createTables(Map<String, ResultSetMetaData> tableDefinition) {
		boolean result = true;
		try (Connection conn = bds.getConnection()) {
			for (String tableName : tableDefinition.keySet()) {
				String createSQL = getCreateSQLFor(tableName, tableDefinition.get(tableName));
				PreparedStatement ps = conn.prepareStatement(createSQL);
				ps.executeUpdate();
				ps.close();

				String tempTable = getTempTableName(tableName);
				createSQL = getCreateSQLFor(tempTable, tableDefinition.get(tableName));
				temporaryTables.add(tempTable);
				ps = conn.prepareStatement(createSQL);
				ps.executeUpdate();
				ps.close();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getCreateSQLFor(String tableName, ResultSetMetaData resultSetMetaData) throws SQLException {
		String sql = """
				create or replace table %s (padl_source_id serial, %s )
				""";

		List<String> cols = new LinkedList<String>();

		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String precision;
			if (resultSetMetaData.getColumnType(i) == Types.DECIMAL
					|| resultSetMetaData.getColumnType(i) == Types.NUMERIC) {
				precision = String.format("(%d,%d)", resultSetMetaData.getPrecision(i), resultSetMetaData.getScale(i));
			} else {
				precision = String.format("(%d)", resultSetMetaData.getPrecision(i));
			}
			cols.add(String.format("%s %s %s", resultSetMetaData.getColumnName(i),
					getMariaDBTypeName(resultSetMetaData.getColumnType(i)), precision));
		}
		String result = String.format(sql, tableName, String.join(",", cols));
		logger.debug("Returning create SQL: %s", result);
		System.out.println(String.format("Returning create SQL: %s", result));
		return result;
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
				create table ldap_oc_mappings
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
				create table ldap_attr_mappings
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
				create table ldap_entries
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
				constraint unq1_ldap_entries unique
				(
					oc_map_id,
					keyval
				)
				""");

		this.sqlUpdate("""
				alter table ldap_entries add
				constraint unq2_ldap_entries unique
				(
					dn
				)
				""");

		this.sqlUpdate("""
				create table ldap_entry_objclasses
				(
					entry_id integer unsigned not null references ldap_entries(id),
					oc_name varchar(64)
				)
				""");

	}

	public void createDNSuffixTable() {
		this.sqlUpdate(String.format("""
				create or replace table %s (suffix_id serial, suffix varchar(100))
				""", SUFFIXES_TABLE));

		this.sqlUpdate(String.format("""
				create or replace table %s (classname varchar(50))
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
		}
	}

	private void sqlUpdate(String sql) {
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
		}

	}

	public void cleanTempTables() {
		try (Connection conn = bds.getConnection()) {
			for (String table : temporaryTables) {
				sqlUpdate(String.format("""
						delete from %s
						""", table));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static class TableDataHelper {
		private String tableName;
		private String query;
		private List<String> columns;
		private String idColumn;

		public TableDataHelper(String tableName, String uidCol, String query, List<String> columns) {
			super();
			this.tableName = tableName;
			this.idColumn = uidCol;
			this.query = query;
			this.columns = columns;
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

		List<TableDataHelper> items = getTableDataHelper();

		for (TableDataHelper item : items) {

			String template = """
					insert into %s ( %s )  values ( %s )
					""";

			String colValues = "?,".repeat(item.getColumns().size());

			colValues = colValues.substring(0, colValues.length() - 1);

			String sqlInsert = String.format(template, getTempTableName(item.getTableName()),
					String.join(",", item.getColumns()), colValues);

			System.out.println("Loading data with: " + sqlInsert);

			try (Connection conn = bds.getConnection()) {
				PreparedStatement psLoad = conn.prepareStatement(sqlInsert);

				ResultSet sourceRs = helper.getDataFor(item.getQuery());

				int count = 0;
				while (sourceRs.next()) {
					for (int i = 0; i < item.getColumns().size(); i++) {
						String col = item.getColumns().get(i);
						psLoad.setObject(i + 1, sourceRs.getObject(col));
					}
					psLoad.addBatch();
					count++;
					if (count > 10_000) {
						psLoad.executeBatch();
						count = 0;
					}
				}

				if (count != 0) {
					psLoad.executeBatch();
				}

				psLoad.close();
				helper.closeResources(sourceRs);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}

	private List<TableDataHelper> getTableDataHelper() {

		LinkedList<TableDataHelper> tableDatum = new LinkedList<TableDataHelper>();

		TableDataHelper tableData = new TableDataHelper(config.getMetaTableName(), config.getIdColumn(),
				config.getQuery(), new LinkedList<String>(config.getDbtoldap().keySet()));

		if (!tableData.getColumns().contains(config.getIdColumn())) {
			tableData.getColumns().add(config.getIdColumn());
		}

		tableDatum.add(tableData);

		config.getJoinData().forEach(item -> {
			List<String> list = new LinkedList<String>(item.getDbtoldap().keySet());
			if (!list.contains(config.getIdColumn())) {
				list.add(config.getIdColumn());
			}
			tableDatum.add(new TableDataHelper(item.getMetaTableName(), item.getIdColumn(), item.getQuery(), list));

		});

		return tableDatum;
	}

	private String getTempTableName(String metaTableName) {
		return metaTableName + "_temp";
	}

	public void mergeData() {

		List<TableDataHelper> tableDatum = getTableDataHelper();

		for (TableDataHelper tableData : tableDatum) {

			try (Connection conn = bds.getConnection()) {
				String proxyTableName = tableData.getTableName();
				String idColumn = tableData.getIdColumn();
				String tempTableName = getTempTableName(proxyTableName);
				List<String> fmtJoinAllCols = tableData.getColumns().stream()
						.map(col -> String.format("proxytable.%s = temptable.%s", col, col))
						.collect(Collectors.toList());

				String allJoinCols = String.join(" and ", fmtJoinAllCols);
				String allCols = String.join(",", tableData.getColumns());

				String sqlDeleteDiff = String.format("""
						delete from %s where padl_source_id in (
						select proxytable.padl_source_id
						from %s as proxytable
						left join %s as temptable
						on
							%s
						where temptable.%s is null
						)
						""", proxyTableName, proxyTableName, tempTableName, allJoinCols, idColumn);

				System.out.println("Runing sync delete phase with: \n" + sqlDeleteDiff);
				PreparedStatement psDelete = conn.prepareStatement(sqlDeleteDiff);
				psDelete.executeUpdate();
				psDelete.close();

				String sqlInsertDiff = String.format("""
						insert into %s ( %s )
						select %s from %s as temptable
						except
						select %s from %s as proxytable
							""", proxyTableName, allCols, allCols, tempTableName, allCols, proxyTableName);

				System.out.println("Runing sync insert phase with: \n" + sqlInsertDiff);

				PreparedStatement psInsert = conn.prepareStatement(sqlInsertDiff);

				psInsert.executeUpdate();
				psInsert.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
