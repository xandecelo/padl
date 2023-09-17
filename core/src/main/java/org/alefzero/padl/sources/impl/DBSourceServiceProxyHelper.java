package org.alefzero.padl.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBSourceServiceProxyHelper {

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
			adminBds.setUsername(params.getDbUsername());
			adminBds.setPassword(params.getDbPassword());
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

		try (Connection conn = adminBds.getConnection()) {
			PreparedStatement ps = conn
					.prepareStatement("select schema_name from information_schema.schemata where schema_name like ?");
			ps.setString(1, getDatabaseBaseName() + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
			rs.close();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		// TODO read all the metadata for database with a base and drop them all
		// TODO create a new database
		// TODO create a new set of tables

	}

	public Boolean createTable(String tableDefinition) {
		// TODO Create a table with definition at proxy database
		return null;
	}

	public void createOpenldapTables() {
		// TODO Create all table structure for openldap
		this.loadOpenldapMappings();
		this.loadOpenldapAttributes();
		this.createLdapSuffixEntry();
	}

	private void createLdapSuffixEntry() {
		// TODO think about it

	}

	private void loadOpenldapMappings() {
		// TODO load all data to oc_mappings from objectClasses

	}

	private void loadOpenldapAttributes() {
		// TODO load all data to attr_mappings from config (?).

	}

}
