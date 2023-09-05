package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceFactorySetup;
import org.alefzero.padl.sources.PadlSourceFactory;
import org.alefzero.padl.sources.PadlSourceService;

public class LdapProxySourceFactory extends PadlSourceFactory {

	private LdapProxySourceFactorySetup factorySetup;

	@Override
	public String getServiceType() {
		return "proxy";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<LdapProxySourceServiceConfig> getServiceConfigType() {
		return LdapProxySourceServiceConfig.class;
	}

	@Override
	public <T extends PadlSourceService> Class<T> getService() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<LdapProxySourceFactorySetup> getSourceFactorySetupClass() {
		return LdapProxySourceFactorySetup.class;
	}

	@Override
	public void setFactorySetup(PadlSourceFactorySetup factorySetup) {
		if (factorySetup instanceof LdapProxySourceFactorySetup) {
			this.factorySetup = (LdapProxySourceFactorySetup) factorySetup;
		} else {
			throw new IllegalArgumentException("Can't convert setup class" + factorySetup.getClass().getName() + " to "
					+ LdapProxySourceFactorySetup.class.getName());
		}
	}

	@Override
	public <T extends PadlSourceFactorySetup> T getFactorySetup() {
		// TODO Auto-generated method stub
		return null;
	}

}
