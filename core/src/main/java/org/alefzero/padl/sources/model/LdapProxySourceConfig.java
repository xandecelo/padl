package org.alefzero.padl.sources.model;

import org.alefzero.padl.config.model.PadlSourceConfig;

public class LdapProxySourceConfig extends PadlSourceConfig {

	private String targetURI;
	private String aclBind;

	public String getTargetURI() {
		return targetURI;
	}

	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}

	public String getAclBind() {
		return aclBind;
	}

	public void setAclBind(String aclBind) {
		this.aclBind = aclBind;
	}

	@Override
	public String toString() {
		return "LdapProxySourceConfig [targetURI=" + targetURI + ", aclBind=" + aclBind + "]";
	}

}
