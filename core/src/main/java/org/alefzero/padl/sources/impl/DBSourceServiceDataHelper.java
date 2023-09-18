package org.alefzero.padl.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBSourceServiceDataHelper {

	private DBSourceConfiguration config;
	private BasicDataSource bds = null;

	public DBSourceServiceDataHelper(DBSourceConfiguration config) {
		this.config = config;
		this.setupDataSource();
	}

	private void setupDataSource() {
		if (bds == null) {
			bds = new BasicDataSource();
			bds.setUrl(config.getSourceJdbcURL());
			bds.setUsername(config.getSourceUsername());
			bds.setPassword(config.getSourcePassword());
			bds.setMaxTotal(10);
			bds.setMinIdle(2);
			bds.setCacheState(false);
		}
	}

	public Map<String, ResultSetMetaData> getTableDefinitions() {
		Map<String, ResultSetMetaData> metalist = new HashMap<String, ResultSetMetaData>();
		try (Connection conn = bds.getConnection()) {
			Map<String, String> queries = new HashMap<String, String>();
			queries.put(config.getMetaTableName(), config.getQuery());
			if (config.getJoinData() != null) {
				config.getJoinData().forEach(item -> queries.put(item.getMetaTableName(), item.getQuery()));
			}
			for (String id : queries.keySet()) {
				String query = queries.get(id);
				PreparedStatement ps = conn.prepareStatement(query);
				ResultSet rs = ps.executeQuery();
				metalist.put(id, rs.getMetaData());
				rs.close();
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return metalist;
	}

	public static void main(String[] args) throws SQLException {
		System.out.println("Running...");
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
		config.setSourceJdbcURL("jdbc:mariadb://dev.local:3306/source");
		config.setSourceUsername("dbuser");
		config.setSourcePassword("userpass");
		config.setQuery("select uid, email, name, surname, phone from users");
//		DBSourceServiceDataHelper test = new DBSourceServiceDataHelper(config);
//		ResultSetMetaData meta = test.getMetadataFor();
//		for (int i = 1; i <= meta.getColumnCount() ; i++) {
//			System.out.printf("%s, %s, %s\n", meta.getColumnName(i), meta.getColumnTypeName(i), meta.getColumnType(i));
//		}
	}
}
