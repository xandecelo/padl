package org.alefzero.padl.sources.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.alefzero.padl.sources.PadlSourceConfiguration;

public class DBSourceConfiguration extends PadlSourceConfiguration {

	private String sourceJdbcURL;
	private String sourceUsername;
	private String sourcePassword;

	private String subtreeCond = "\"ldap_entries.dn LIKE CONCAT('%',?)\"";
	private String insertEntryStatement = "\"insert into ldap_entries (dn,oc_map_id,parent,keyval) values (?,?,?,?)\"";

	private boolean batchMode = true;

	private String dnFormat;
	private String query;
	private String idColumn;
	private String[] attributes;
	private List<JoinData> joinData;
	private List<String> objectClasses;
	private Integer dbBatchSize = 100;
	private Boolean testLoadMode = false;

	private Map<String, String> ldaptodb = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> dbtoldap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

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

	public boolean getBatchMode() {
		return batchMode;
	}

	public void setBatchMode(boolean batchMode) {
		this.batchMode = batchMode;
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
		getLdaptodb().clear();
		getDbtoldap().clear();
		for (String att : attributes) {
			String[] split = att.split("=");
			String ldap = split[0];
			String db = split.length == 1 ? ldap : split[1];
			getLdaptodb().put(ldap, db);
			getDbtoldap().put(db, ldap);
		}
	}

	public List<JoinData> getJoinData() {
		return joinData != null ? joinData : new LinkedList<JoinData>();
	}

	public void setJoinData(List<JoinData> joinData) {
		joinData.forEach(item -> item.setOuterId(this.getId()));
		this.joinData = joinData;
	}

	public List<String> getObjectClasses() {
		return objectClasses;
	}

	public void setObjectClasses(List<String> objectClasses) {
		this.objectClasses = objectClasses;
	}

	public String getMetaTableName() {
		return this.getId();
	}

	public String getMainObjectClass() {
		return this.objectClasses.get(0);
	}

	public static class JoinData {

		private String outerId = "";

		private String id;
		private String query;
		private String joinColumns;
		private String[] attributes;
		private String idColumn;
		private Map<String, String> ldaptodb = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		private Map<String, String> dbtoldap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String[] getAttributes() {
			return attributes;
		}

		public void setAttributes(String[] attributes) {
			this.attributes = attributes;
			getLdaptodb().clear();
			getDbtoldap().clear();
			for (String att : attributes) {
				String[] split = att.split("=");
				String ldap = split[0];
				String db = split.length == 0 ? ldap : split[1];
				getLdaptodb().put(ldap, db);
				getDbtoldap().put(db, ldap);
			}
		}

		public String getMetaTableName() {
			return String.format("%s_%s", outerId, id);
		}

		public String getOuterId() {
			return outerId;
		}

		public void setOuterId(String outerId) {
			this.outerId = outerId;
		}

		public Map<String, String> getLdaptodb() {
			return ldaptodb;
		}

		public void setLdaptodb(Map<String, String> ldaptodb) {
			this.ldaptodb = ldaptodb;
		}

		public Map<String, String> getDbtoldap() {
			return dbtoldap;
		}

		public void setDbtoldap(Map<String, String> dbtoldap) {
			this.dbtoldap = dbtoldap;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getJoinColumns() {
			return joinColumns;
		}

		public void setJoinColumns(String idJoinColumnFilter) {
			this.joinColumns = idJoinColumnFilter;
		}

		public String getJoinColumnFromSource() {
			return joinColumns.split("=")[0];
		}

		public String getJoinColumnFromJoin() {
			String[] split = joinColumns.split("=");
			return split.length == 1 ? split[0] : split[1];
		}

		@Override
		public String toString() {
			return "JoinData [outerId=" + outerId + ", id=" + id + ", query=" + query + ", joinColumns=" + joinColumns
					+ ", attributes=" + Arrays.toString(attributes) + ", ldaptodb=" + ldaptodb + ", dbtoldap="
					+ dbtoldap + "]";
		}

		public String getIdColumn() {
			return idColumn;
		}

		public void setIdColumn(String idColumn) {
			this.idColumn = idColumn;
		}

	}

	public Boolean getTestLoadMode() {
		return testLoadMode;
	}

	public void setTestLoadMode(Boolean testLoadMode) {
		this.testLoadMode = testLoadMode;
	}


	public Integer getDbBatchSize() {
		return dbBatchSize;
	}

	public void setDbBatchSize(Integer dbBatchSize) {
		this.dbBatchSize = dbBatchSize;
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
		sb.append("\nolcDbName: ").append(this.getDatabaseFullName());
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
		sb.append(" DB_DATABASE=").append(this.getDatabaseFullName());
		sb.append(" DB_PORT=").append(params.getDbPort());
		return sb.toString();
	}

	private Integer getInstanceNumberId() {
		Integer result = 0;
		try {
			String mode = System.getenv("DEV_MODE");
			Path file = Paths.get("/opt/app/db_service_id");
			if (mode != null && "true".equalsIgnoreCase(mode)) {
				Files.write(file, "000".getBytes("UTF-8"));
			}
			if (!Files.exists(file)) {
				String randomId = String.format("%03d", (new Random()).nextInt(999));
				Files.write(file, randomId.getBytes());
			}
			byte[] data = Files.readAllBytes(file);
			result = Integer.parseInt(new String(data).trim());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	protected String getDatabaseBaseName() {
		return String.format("%s_%s", this.getInstanceId(), this.getId());
	}

	protected String getDatabaseFullName() {
		return String.format("%s_%03d", this.getDatabaseBaseName(), getInstanceNumberId());
	}

	public String getSuffixType() {
		String simpleSuffix = getSuffix().replace(getBaseSuffix(), "");
		return simpleSuffix.split("=")[0];
	}

	public String getSuffixName() {
		String simpleSuffix = getSuffix().replace(getBaseSuffix(), "");
		return simpleSuffix.split("=")[1].replace(",", "");
	}

	public Map<String, String> getLdaptodb() {
		return ldaptodb;
	}

	public void setLdaptodb(Map<String, String> ldaptodb) {
		this.ldaptodb = ldaptodb;
	}

	public Map<String, String> getDbtoldap() {
		return dbtoldap;
	}

	public void setDbtoldap(Map<String, String> dbtoldap) {
		this.dbtoldap = dbtoldap;
	}

	public List<String> getExtraClasses() {
		List<String> list = new LinkedList<String>(getObjectClasses());
		list.remove(0);
		return list;
	}

}
