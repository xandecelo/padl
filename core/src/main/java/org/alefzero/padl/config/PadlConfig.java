package org.alefzero.padl.config;

import java.nio.file.Path;

public class PadlConfig {

	private Path configurationFile;

	public PadlConfig(Path configurationFile) {
		this.configurationFile = configurationFile;
	}

	public String getTargetAdminConfig() {
		return new StringBuffer().append("# Change cn=admin,cn=config password")
				.append("\ndn: olcDatabase={0}config,cn=config").append("\nchangetype: modify")
				.append("\nreplace: olcRootPW").append("\nolcRootPW: %%LDAP_ROOT_PASSWORD%%").append("\n").toString();
	}

}
