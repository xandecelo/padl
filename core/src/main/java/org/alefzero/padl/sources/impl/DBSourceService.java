package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBSourceService extends PadlSourceService {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public void sync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void prepare() {
		logger.debug("Preparing sync for {} with id {}.", this.getConfig().getType(), this.getConfig().getId());
		System.out.printf("Preparing sync for %s with id %s\n.", this.getConfig().getType(), this.getConfig().getId());
		createOpenLdapTables();
		createPadlInfoTables();
		insertObjectClasses();
		insertAttributes();
	}

	private void createPadlInfoTables() {
		// TODO Auto-generated method stub
		
	}

	private void insertAttributes() {
		// TODO Auto-generated method stub
		
	}

	private void insertObjectClasses() {
		// TODO Auto-generated method stub
		
	}

	private void createOpenLdapTables() {
		// TODO Auto-generated method stub

	}

}
