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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceServiceProxyHelper {
	protected static final Logger logger = LogManager.getLogger();

	private static final Integer SUFFIX_OBJECT_CLASS_ID = 1;

	private static final int PROXY_OBJECT_CLASS_ID = 2;

	private static final String SUFFIXES_TABLE = "suffixes";

	private BasicDataSource adminBds = null;
	private BasicDataSource bds = null;
	private DBSourceParameters params;
	private DBSourceConfiguration config;
	private static Integer randomId = new Random().nextInt(99_999);

	private Map<Integer, String> objectClasses = new HashMap<Integer, String>();

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

	public boolean createTables(Map<String, ResultSetMetaData> td) {
		boolean result = true;
		try (Connection conn = bds.getConnection()) {
			for (String tableName : td.keySet()) {
				String createSQL = getSQLFor(tableName, td.get(tableName));
				PreparedStatement ps = conn.prepareStatement(createSQL);
				ps.executeUpdate();
				ps.close();

				createSQL = getSQLFor(tableName + "_temp", td.get(tableName));
				ps = conn.prepareStatement(createSQL);
				ps.executeUpdate();
				ps.close();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getSQLFor(String tableName, ResultSetMetaData resultSetMetaData) throws SQLException {
		String sql = """
				create or replace table %s (padl_source_id serial, %s )
				""";

		List<String> cols = new LinkedList<String>();

		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String precision;
			if (resultSetMetaData.getScale(i) > 0) {
				precision = String.format("(%d,%d)", resultSetMetaData.getPrecision(i), resultSetMetaData.getScale(i));
			} else {
				precision = String.format("(%d)", resultSetMetaData.getPrecision(i));
			}
			cols.add(String.format("%s %s %s", resultSetMetaData.getColumnName(i),
					getMariaDBTypeName(resultSetMetaData.getColumnType(i)), precision));
		}
		return String.format(sql, tableName, String.join(",", cols));
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
		this.sqlUpdate(

				String.format("""
						create or replace table %s (suffix_id serial, suffix varchar(100))
						""", SUFFIXES_TABLE));

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
			ps.setString(1, config.getSuffix());
			ps.executeUpdate();
			ps.close();
			
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

}
