package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceParameters;

public class DBSourceFactory extends PadlSourceFactory {

	private DBSourceFactorySetup sourceParameters = null;

	@Override
	public String getServiceType() {
		return "sql";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceConfig> getSourceConfigType() {
		return DBSourceConfig.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DBSourceService getService() {
		return new DBSourceService();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DBSourceFactorySetup getSourceParameters() {
		return this.sourceParameters;
	}

	@Override
	public void setSourceParameters(PadlSourceParameters sourceParameters) {
		if (sourceParameters instanceof DBSourceFactorySetup) {
			this.sourceParameters = (DBSourceFactorySetup) sourceParameters;
		} else {
			throw new IllegalArgumentException("Can't convert setup class" + sourceParameters.getClass().getName() + " to "
					+ DBSourceFactorySetup.class.getName());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceFactorySetup> getSourceParameterClassType() {
		return DBSourceFactorySetup.class;
	}

}
