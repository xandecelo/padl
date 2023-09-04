package org.alefzero.padl.config.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PadlGeneralConfig {

	private boolean sourcesSorted = false;

	private String instanceId;
	private String lang;
	private String rootDn;
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

	public String getRootDn() {
		return rootDn;
	}

	public PadlGeneralConfig setRootDn(String rootDn) {
		this.rootDn = rootDn;
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
		if (!sourcesSorted) {
			Collections.sort(sources);
			sourcesSorted = true;
		}
		return sources;
	}

	public PadlGeneralConfig setSources(List<PadlSourceConfig> sources) {
		this.sources = sources;
		return this;
	}

	@Override
	public String toString() {
		return "PadlConfig [instanceId=" + instanceId + ", lang=" + lang + ", rootDn=" + rootDn + ", adminPassword="
				+ adminPassword + "]";
	}

}
