package org.alefzero.padl.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alefzero.padl.sources.PadlSourceConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PadlConfig {

	protected static final Logger logger = LogManager.getLogger();

	private boolean sourcesSorted = false;

	private String instanceId;
	private String lang;
	private String suffix;
	private String adminPassword;
	private List<PadlSourceConfiguration> sources = new LinkedList<PadlSourceConfiguration>();

	public String getInstanceId() {
		return instanceId;
	}

	public PadlConfig setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	public String getLang() {
		return lang;
	}

	public PadlConfig setLang(String lang) {
		this.lang = lang;
		return this;
	}

	public String getSuffix() {
		return suffix;
	}

	public PadlConfig setSuffix(String suffix) {
		this.suffix = suffix;
		return this;

	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public PadlConfig setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		return this;
	}

	public List<PadlSourceConfiguration> getSources() {
		this.sources.forEach(source -> source.setInstanceId(this.getInstanceId()));
		return sources;
	}

	public PadlConfig setSources(List<PadlSourceConfiguration> sources) {
		this.sources = sources;
		this.sources.forEach(source -> source.setInstanceId(this.getInstanceId()));
		this.sources.forEach(source -> source.setBaseSuffix(this.getSuffix()));
		return this;
	}

	public String getLDIFSetupConfiguration() {
		StringBuffer sb = new StringBuffer();
		sb.append(getRootDNPasswordChange());
		sb.append("\n\n");

		sb.append(getDeleteDefaultPassword());
		sb.append("\n\n");


		boolean addDefaultMdbBack = true;

		for (PadlSourceConfiguration source : this.getSourcesInConfigurationOrder()) {
			logger.trace("Processing source [{}, {}] to configuration LDIF", source.getId(), source.getType());
			if (this.getSuffix().equalsIgnoreCase(source.getSuffix())) {
				sb.append("\n").append(source.getConfigurationLDIF().trim());
				sb.append("\nolcRootPW: ").append(this.getAdminPassword());
				addDefaultMdbBack = false;
			} else {
				sb.append(source.getConfigurationLDIF().trim());
				sb.append("\nolcSubordinate: true");
			}
			sb.append("\nolcAccess: to attrs=userPassword by self write by anonymous auth by * none");
			sb.append("\nolcAccess: to attrs=shadowLastChange by self write by * read");
			sb.append("\nolcAccess: to * by * read");

			sb.append("\n\n");
		}
		
		sb.append(getDeleteDefaultMdbLDIF());
		sb.append("\n\n");

		if (addDefaultMdbBack) {
			sb.append(getDefaultMdbConfiguration());
		}

		sb.append("\n\n");

		return sb.toString();
	}

	public String getRootDNPasswordChange() {
		StringBuffer sb = new StringBuffer().append("# Change cn=admin,cn=config password");
		sb.append("\ndn: olcDatabase={0}config,cn=config");
		sb.append("\nchangetype: modify");
		sb.append("\nreplace: olcRootPW");
		sb.append("\nolcRootPW: %%LDAP_ROOT_PASSWORD%%");
		return sb.toString();
	}

	private String getDeleteDefaultPassword() {
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
				dn: olcDatabase=mdb,cn=config
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

	public List<PadlSourceConfiguration> getSourcesInConfigurationOrder() {
		if (!sourcesSorted) {
			sources.forEach(source -> source
					.setRootDN(source.getRootDN() == null ? "cn=admin," + this.getSuffix() : source.getRootDN()));
			Collections.sort(sources);
			sourcesSorted = true;
		}
		return sources;
	}

	@Override
	public String toString() {
		return "PadlConfig [sourcesSorted=" + sourcesSorted + ", instanceId=" + instanceId + ", lang=" + lang
				+ ", suffix=" + suffix + ", adminPassword=" + "***********" + ", sources=" + sources + "]";
	}

	public String getSourceOSConfig() {
		StringBuffer sb = new StringBuffer();

		for (PadlSourceConfiguration source : this.getSourcesInConfigurationOrder()) {
			if (source.getOSRequirementScript() != null) {
				sb.append(source.getOSRequirementScript()).append("\n");
			}
		}
		return sb.toString();
	}

}
