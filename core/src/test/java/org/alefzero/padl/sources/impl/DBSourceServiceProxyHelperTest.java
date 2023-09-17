package org.alefzero.padl.sources.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;

@TestInstance(Lifecycle.PER_CLASS)
public class DBSourceServiceProxyHelperTest {
	protected static final Logger logger = LogManager.getLogger();
	private DBSourceParameters params;
	private DBSourceConfiguration config;

	@BeforeAll
	void prepare() {
			params = new DBSourceParameters();
			params.setDbDatabase("sql");
			params.setDbUsername("dbuser");
			params.setDbPassword("userpass");
			params.setDbServer("dev.local");
			params.setDbPort(3306);
			
			config = new DBSourceConfiguration();
			config.setInstanceId("t");
			config.setId("source1");
			
	}
	@Test
	void checkDatabase() {
		DBSourceServiceProxyHelper test = new DBSourceServiceProxyHelper(params, config);
		assertDoesNotThrow(new Executable() {
			@Override
			public void execute() throws Throwable {
				test.cleanDatabases();
			}
		});
	}

}
