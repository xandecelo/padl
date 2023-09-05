package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSource;
import org.alefzero.padl.sources.PadlSourceFactory;

public class LdapProxySourceFactory  implements PadlSourceFactory {

	@Override
	public String getServiceType() {
		return "proxy";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<LdapProxySourceConfig> getConfigClass() {
		return LdapProxySourceConfig.class;
	}

	@Override
	public <T extends PadlSource> Class<T> getService() {
		// TODO Auto-generated method stub
		return null;
	}

}
