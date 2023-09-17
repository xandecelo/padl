package org.alefzero.padl.sources.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.alefzero.padl.sources.PadlSourceConfiguration;

public class DBSourceConfiguration extends PadlSourceConfiguration {

	private String sourceJdbcURL;
	private String sourceUsername;
	private String sourcePassword;

	private String subtreeCond = "\"ldap_entries.dn LIKE CONCAT('%',?)\"";
	private String insertEntryStatement = "\"insert into ldap_entries (dn,oc_map_id,parent,keyval) values (?,?,?,?)\"";

	private String dnFormat;
	private String query;
	private String idColumn;
	private String[] attributes;
	private List<JoinData> joinData;
	private List<String> objectClasses;
	
	private static Integer randomId = new Random().nextInt(99_999);

	public String getSourceJdbcURL() {
		return sourceJdbcURL;
	}

	public void setSourceJdbcURL(String sourceJdbcURL) {
		this.sourceJdbcURL = sourceJdbcURL;
	}

	public String getSourceUsername() {
		return sourceUsername;
	}

	public void setSourceUsername(String sourceUsername) {
		this.sourceUsername = sourceUsername;
	}

	public String getSourcePassword() {
		return sourcePassword;
	}

	public void setSourcePassword(String sourcePassword) {
		this.sourcePassword = sourcePassword;
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

	@Override
	public String getOSEnv() {
		var sb = new StringBuffer();
		DBSourceParameters params = this.getFactory().getSourceParameters();
		sb.append("DB_SERVER=").append(params.getDbServer());
		sb.append(" DB_USERNAME=").append(params.getDbUsername());
		sb.append(" DB_PASSWORD=").append(params.getDbPassword());
		sb.append(" DB_DATABASE=").append(this.getDbDatabaseId());
		sb.append(" DB_PORT=").append(params.getDbPort());
		return sb.toString();
	}

	protected String getDbDatabaseBase() {
		return String.format("%s_%s", ((DBSourceParameters) this.getFactory().getSourceParameters()).getDbDatabase(),
				this.getId());
	}
	
	protected String getDbDatabaseId() {
		return String.format("%s_%s_%5d", ((DBSourceParameters) this.getFactory().getSourceParameters()).getDbDatabase(),
				this.getId(), randomId);
	}
	
}
