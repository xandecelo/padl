package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceParameters;

public class LdapProxySourceFactory extends PadlSourceFactory {

	private LdapProxySourceParameters sourceParameters;

	@Override
	public String getServiceType() {
		return "proxy";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<LdapProxySourceConfiguration> getSourceConfigType() {
		return LdapProxySourceConfiguration.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public LdapProxySourceService getService() {
		return new LdapProxySourceService();
	}

	@Override
	@SuppressWarnings("unchecked")
	public LdapProxySourceParameters getSourceParameters() {
		return this.sourceParameters;
	}

	@Override
	public void setSourceParameters(PadlSourceParameters factorySetup) {
		if (factorySetup instanceof LdapProxySourceParameters) {
			this.sourceParameters = (LdapProxySourceParameters) factorySetup;
		} else {
			throw new IllegalArgumentException("Can't convert setup class" + factorySetup.getClass().getName() + " to "
					+ LdapProxySourceParameters.class.getName());
		}
	}

	@Override
	public <T extends PadlSourceParameters> Class<T> getSourceParameterClassType() {
		// TODO Auto-generated method stub
		return null;
	}


}
