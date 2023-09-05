package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceFactorySetup;
import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceService;

public class DBSourceFactory extends PadlSourceFactory {

	private DBSourceFactorySetup factorySetup = null;

	@Override
	public String getServiceType() {
		return "sql";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceConfig> getServiceConfigType() {
		return DBSourceConfig.class;
	}

	@Override
	public <T extends PadlSourceService> Class<T> getService() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceFactorySetup> getSourceFactorySetupClass() {
		return DBSourceFactorySetup.class;
	}

	@Override
	public <T extends PadlSourceFactorySetup> T getFactorySetup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFactorySetup(PadlSourceFactorySetup factorySetup) {
		if (factorySetup instanceof DBSourceFactorySetup) {
			this.factorySetup = (DBSourceFactorySetup) factorySetup;
		} else {
			throw new IllegalArgumentException("Can't convert setup class" + factorySetup.getClass().getName() + " to "
					+ DBSourceFactorySetup.class.getName());
		}
	}

}
