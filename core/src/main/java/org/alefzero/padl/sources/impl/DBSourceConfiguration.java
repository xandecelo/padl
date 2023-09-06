package org.alefzero.padl.sources.impl;

import java.util.Arrays;
import java.util.List;

import org.alefzero.padl.sources.PadlSourceConfiguration;

public class DBSourceConfiguration extends PadlSourceConfiguration {

	private String jdbcURL;

	private String username;
	private String password;

	private String subtreeCond = "\"ldap_entries.dn LIKE CONCAT('%',?)\"";
	private String insertEntryStatement = "\"insert into ldap_entries (dn,oc_map_id,parent,keyval) values (?,?,?,?)\"";

	private String dnFormat;
	private String query;
	private String idColumn;
	private String[] attributes;
	private List<JoinData> joinData;
	private List<String> objectClasses;

	public String getJdbcURL() {
		return jdbcURL;
	}

	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSubtreeCond() {
		return subtreeCond;
	}

	public void setSubtreeCond(String subtreeCond) {
		this.subtreeCond = subtreeCond;
	}

	public String getInsertEntryStatement() {
		return insertEntryStatement;
	}

	public void setInsertEntryStatement(String insertEntryStatement) {
		this.insertEntryStatement = insertEntryStatement;
	}

	public String getDnFormat() {
		return dnFormat;
	}

	public void setDnFormat(String dnFormat) {
		this.dnFormat = dnFormat;
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

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public List<JoinData> getJoinData() {
		return joinData;
	}

	public void setJoinData(List<JoinData> joinData) {
		this.joinData = joinData;
	}

	public List<String> getObjectClasses() {
		return objectClasses;
	}

	public void setObjectClasses(List<String> objectClasses) {
		this.objectClasses = objectClasses;
	}

	public static class JoinData {

		private String id;
		private String query;
		private String idJoinColumnFilter;
		private String[] attributes;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getIdJoinColumnFilter() {
			return idJoinColumnFilter;
		}

		public void setIdJoinColumnFilter(String idJoinColumnFilter) {
			this.idJoinColumnFilter = idJoinColumnFilter;
		}

		public String[] getAttributes() {
			return attributes;
		}

		public void setAttributes(String[] attributes) {
			this.attributes = attributes;
		}

		@Override
		public String toString() {
			return "JoinData [id=" + id + ", query=" + query + ", idJoinColumnFilter=" + idJoinColumnFilter
					+ ", attributes=" + Arrays.toString(attributes) + "]";
		}

	}

	@Override
	public String getConfigurationLDIF() {
		DBSourceParameters params = (DBSourceParameters) this.getSource().getSourceParameters();
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n# Back-sql ldap configuration (").append(this.getId()).append(")");
		sb.append("\ndn: olcDatabase=sql,cn=config");
		sb.append("\nchangetype: add");
		sb.append("\nobjectClass: olcDatabaseConfig");
		sb.append("\nobjectClass: olcSqlConfig");
		sb.append("\nolcDatabase: sql");
		sb.append("\nolcSuffix: ").append(this.getSuffix());
		sb.append("\nolcRootDN: ").append(this.getRootDN());
		sb.append("\nolcDbName: ").append(params.getDbDatabase());
		sb.append("\nolcDbUser: ").append(params.getDbUsername());
		sb.append("\nolcDbPass: ").append(params.getDbPassword());
		sb.append("\nolcSqlSubtreeCond: ").append(this.getSubtreeCond());
		sb.append("\nolcSqlInsEntryStmt: ").append(this.getInsertEntryStatement());
		return sb.toString();
	}

	public boolean hasOSPreScript() {
		return true;
	}

}
