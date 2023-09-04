package org.alefzero.padl.sources;

import org.alefzero.padl.config.model.PadlSource;
import org.alefzero.padl.config.model.PadlSourceFactory;
import org.alefzero.padl.sources.model.LdapProxySourceConfig;

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
