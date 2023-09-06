package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceParameters;

public class DBSourceFactory extends PadlSourceFactory {

	private DBSourceParameters sourceParameters = new DBSourceParameters();

	@Override
	public String getServiceType() {
		return "sql";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceConfiguration> getSourceConfigType() {
		return DBSourceConfiguration.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DBSourceService getService() {
		return new DBSourceService();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DBSourceParameters getSourceParameters() {
		return this.sourceParameters;
	}

	@Override
	public void setSourceParameters(PadlSourceParameters sourceParameters) {
		if (sourceParameters instanceof DBSourceParameters) {
			this.sourceParameters = (DBSourceParameters) sourceParameters;
		} else {
			throw new IllegalArgumentException("Can't convert setup class" + sourceParameters.getClass().getName() + " to "
					+ DBSourceParameters.class.getName());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<DBSourceParameters> getSourceParameterClassType() {
		return DBSourceParameters.class;
	}

}
