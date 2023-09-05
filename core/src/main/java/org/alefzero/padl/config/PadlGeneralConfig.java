package org.alefzero.padl.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.sources.PadlSourceConfig;

public class PadlGeneralConfig {

	private boolean sourcesSorted = false;

	private String instanceId;
	private String lang;
	private String suffix;
	private String adminPassword;
	private List<PadlSourceConfig> sources = new LinkedList<PadlSourceConfig>();

	public String getInstanceId() {
		return instanceId;
	}

	public PadlGeneralConfig setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	public String getLang() {
		return lang;
	}

	public PadlGeneralConfig setLang(String lang) {
		this.lang = lang;
		return this;
	}

	public String getSuffix() {
		return suffix;
	}

	public PadlGeneralConfig setSuffix(String suffix) {
		this.suffix = suffix;
		return this;

	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public PadlGeneralConfig setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		return this;
	}

	public List<PadlSourceConfig> getSources() {
		return sources;
	}

	public PadlGeneralConfig setSources(List<PadlSourceConfig> sources) {
		this.sources = sources;
		return this;
	}

	public String getLDIFConfiguration() {
		StringBuffer sb = new StringBuffer();

		sb.append(deleteDefaultPassword());
		sb.append("\n\n");

		sb.append(getDeleteDefaultMdbLDIF());
		sb.append("\n\n");

		boolean addDefaultMdbBack = true;

		for (PadlSourceConfig item : this.getSourcesInConfigurationOrder()) {
			if (this.getSuffix().equalsIgnoreCase(item.getSuffix())) {
				sb.append("\n").append(item.getConfigurationLDIF().trim());
				sb.append("olcRootPW: ").append(this.getAdminPassword());
				addDefaultMdbBack = false;
			} else {
				sb.append(item.getConfigurationLDIF());
			}
			sb.append("\n\n");
		}

		if (addDefaultMdbBack) {
			sb.append(getDefaultMdbConfiguration());
		}

		sb.append("\n\n");

		return sb.toString();
	}

	private String deleteDefaultPassword() {
		return """
				# Delete default password of this ldap
				dn: olcDatabase={1}mdb,cn=config
				changetype: modify
				delete: olcRootPW

				""";
	}

	private String getDefaultMdbConfiguration() {

		return String.format("""
				# Default MDB configuration
				dn: olcDatabase={1}mdb,cn=config
				objectClass: olcDatabaseConfig
				objectClass: olcMdbConfig
				olcDatabase: {1}mdb
				olcDbDirectory: /etc/ldap/slapd.d
				olcAccess: {0}to attrs=userPassword by self write by anonymous auth by * none
				olcAccess: {1}to attrs=shadowLastChange by self write by * read
				olcAccess: {2}to * by * read
				olcDbCheckpoint: 512 30
				olcDbIndex: cn,uid eq
				olcDbIndex: member,memberUid eq
				olcDbIndex: objectClass eq
				olcDbIndex: uidNumber,gidNumber eq
				olcDbMaxSize: 1073741824
				olcLastMod: TRUE
				olcRootDN: %s
				olcRootPW: %s
				olcSuffix: %s
				""", "cn=admin," + this.getSuffix(), this.getAdminPassword(), this.getSuffix());

	}

	private String getDeleteDefaultMdbLDIF() {
		return """
				# Delete default MDB database from ldap
				dn: olcDatabase={1}mdb,cn=config
				changetype: delete
				""";
	}

	private List<PadlSourceConfig> getSourcesInConfigurationOrder() {
		if (!sourcesSorted) {
			sources.forEach(
					source -> source.setRootDN(source.getRootDN() == null ? this.getSuffix() : source.getRootDN()));
			Collections.sort(sources);
			sourcesSorted = true;
		}
		return sources;
	}

	@Override
	public String toString() {
		return "PadlConfig [instanceId=" + instanceId + ", lang=" + lang + ", suffix=" + suffix + ", adminPassword="
				+ adminPassword + "]";
	}

}
