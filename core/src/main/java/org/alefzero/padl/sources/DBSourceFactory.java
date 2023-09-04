package org.alefzero.padl.sources;

import org.alefzero.padl.config.model.PadlSource;
import org.alefzero.padl.config.model.PadlSourceFactory;
import org.alefzero.padl.sources.model.DBSourceConfig;

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
		// TODO Auto-generated method stub
		return null;
	}

}
