package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceParameters;

public class DBSourceFactorySetup extends PadlSourceParameters {

	private String dbUsername;
	private String dbPassword;
	private String dbServer;
	private String dbPort;
	private String dbDatabase;

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

	public String getDbPort() {
		return dbPort;
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbDatabase() {
		return dbDatabase;
	}

	public void setDbDatabase(String dbDatabase) {
		this.dbDatabase = dbDatabase;
	}

}
