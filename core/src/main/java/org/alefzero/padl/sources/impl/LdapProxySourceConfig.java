package org.alefzero.padl.sources.impl;

import org.alefzero.padl.sources.PadlSourceConfig;

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

	@Override
	public String getConfigurationLDIF() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n# Ldap proxy configuration (").append(this.getId()).append(")");
		sb.append("\ndn: olcDatabase=ldap,cn=config");
		sb.append("\nobjectClass: olcDatabaseConfig");
		sb.append("\nobjectClass: olcLDAPConfig");
		sb.append("\nolcDatabase: ldap");
		sb.append("\nolcRootDN: ").append(super.getRootDN());
		sb.append("\nolcSuffix: ").append(super.getSuffix());
		sb.append("\nolcDbURI: ").append(this.getTargetURI());
		sb.append("\nolcDbACLBind: ").append(this.getAclBind());
		sb.append("\n").toString();
		return sb.toString();
	}

}
