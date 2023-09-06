package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceParameters;

public class DBSourceParameters extends PadlSourceParameters {

	private String dbUsername = "root";
	private String dbPassword = "";
	private String dbServer = "localhost";
	private Integer dbPort = 3306;
	private String dbDatabase = "default";

	public String getDbUsername() {
		return dbUsername;
	}

	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbServer() {
		return dbServer;
	}

	public void setDbServer(String dbServer) {
		this.dbServer = dbServer;
	}

	public Integer getDbPort() {
		return dbPort;
	}

	public void setDbPort(Integer dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbDatabase() {
		return dbDatabase;
	}

	public void setDbDatabase(String dbDatabase) {
		this.dbDatabase = dbDatabase;
	}

	@Override
	public String getOSEnv() {
		var sb = new StringBuffer();
		sb.append("DB_SERVER=").append(dbServer);
		sb.append(" DB_USERNAME=").append(dbUsername);
		sb.append(" DB_PASSWORD=").append(dbPassword);
		sb.append(" DB_DATABASE=").append(dbDatabase);
		sb.append(" DB_PORT=").append(dbPort);
		return sb.toString();
	}

}
