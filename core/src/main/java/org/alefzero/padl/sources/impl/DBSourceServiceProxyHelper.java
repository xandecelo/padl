package org.alefzero.padl.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceServiceProxyHelper {
	protected static final Logger logger = LogManager.getLogger();

	private BasicDataSource adminBds = null;
	private BasicDataSource bds = null;
	private DBSourceParameters params;
	private DBSourceConfiguration config;
	private static Integer randomId = new Random().nextInt(99_999);

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
		return String.format("%s_%5d", getDatabaseBaseName(), randomId);
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

	public Boolean createTable(String tableDefinition) {
		// TODO Create a table with definition at proxy database
		return null;
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
		
		this.loadOpenldapMappings();
		this.loadOpenldapAttributes();
		this.createLdapSuffixEntry();
	}

	private void createLdapSuffixEntry() {
		// TODO think about it

	}

	private void loadOpenldapMappings() {

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

	private void loadOpenldapAttributes() {
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
		DBSourceServiceProxyHelper test = new DBSourceServiceProxyHelper(params, config);
		test.cleanDatabases();
		test.createOpenldapTables();
	}

}
