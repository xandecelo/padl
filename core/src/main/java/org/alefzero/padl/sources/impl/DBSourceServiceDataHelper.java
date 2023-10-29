package org.alefzero.padl.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceServiceDataHelper {
	protected static final Logger logger = LogManager.getLogger();

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

	private class QueryItem {
		private String query;
		private String idColumn;

		public QueryItem(String query, String idColumn) {
			this.setQuery(query);
			this.setIdColumn(idColumn);
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getIdColumn() {
			return idColumn;
		}

		public void setIdColumn(String idColumn) {
			this.idColumn = idColumn;
		}

	}

	public Map<String, DBSourceMeta> getTableDefinitions() {
		Map<String, DBSourceMeta> metalist = new HashMap<String, DBSourceMeta>();
		try (Connection conn = bds.getConnection()) {
			Map<String, QueryItem> queries = new HashMap<String, QueryItem>();

			queries.put(config.getMetaTableName(), new QueryItem(config.getQuery(), config.getIdColumn()));
			logger.trace("Getting table metadata information for {} -> {}", config.getMetaTableName(),
					config.getQuery());
			if (config.getJoinData() != null) {
				config.getJoinData().forEach(item -> queries.put(item.getMetaTableName(),
						new QueryItem(item.getQuery(), config.getIdColumn())));
			}
			for (String id : queries.keySet()) {
				QueryItem query = queries.get(id);
				PreparedStatement ps = conn.prepareStatement(query.getQuery());
				ResultSet rs = ps.executeQuery();
				metalist.put(id, new DBSourceMeta(rs.getMetaData(), query.getIdColumn()));
				rs.close();
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return metalist;
	}

	public ResultSet getDataFor(String query) throws SQLException {
		Connection conn = bds.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		return ps.executeQuery();
	}

	public void closeResources(ResultSet rs) throws SQLException {
		Connection conn = rs.getStatement().getConnection();
		rs.close();
		conn.close();
	}

}
