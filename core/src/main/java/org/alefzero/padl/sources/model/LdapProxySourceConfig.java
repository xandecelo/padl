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

	@Override
	public String getConfigurationLDIF() {
		StringBuffer sb = new StringBuffer();
		return sb.append("\n\n# Ldap proxy configuration (").append(this.getId())
				.append(")\ndn: olcDatabase=ldap,cn=config").append("\nobjectClass: olcDatabaseConfig")
				.append("\nobjectClass: olcLDAPConfig").append("\nolcDatabase: ldap").append("\nolcRootDN: ")
				.append(super.getRootDN()).append("\nolcSuffix:").append(super.getSuffix()).append("\nolcDbURI: ")
				.append(this.getTargetURI()).append("\nolcDbACLBind: ").append(this.getAclBind()).append("\n")
				.toString();
	}

}
