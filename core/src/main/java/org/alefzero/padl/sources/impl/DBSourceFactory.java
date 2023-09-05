package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSource;
import org.alefzero.padl.sources.PadlSourceFactory;

public class DBSourceFactory implements PadlSourceFactory {

	@Override
	public String getServiceType() {
		return "sql";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceConfig> getConfigClass() {
		return DBSourceConfig.class;
	}

	@Override
	public <T extends PadlSource> Class<T> getService() {
		return null;
	}

}
