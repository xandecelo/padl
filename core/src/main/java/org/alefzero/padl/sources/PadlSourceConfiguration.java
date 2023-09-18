package org.alefzero.padl.sources;

import java.util.Objects;

public abstract class PadlSourceConfiguration implements Comparable<PadlSourceConfiguration> {

	private PadlSourceFactory factory = null;
	private PadlSourceService source = null;

	public abstract String getConfigurationLDIF();

	private String instanceId;

	private String id;
	private String type;
	private String suffix;
	private String reversedSuffix;
	private String rootDN;
	private Boolean enabled = true;
	private String baseSuffix = "";

	public String getRootDN() {
		return rootDN;
	}

	public void setRootDN(String rootDN) {
		this.rootDN = rootDN;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getReversedSuffix() {
		return reversedSuffix;
	}

	public void setReversedSuffix(String reversedSuffix) {
		this.reversedSuffix = reversedSuffix;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public PadlSourceConfiguration setSuffix(String suffix) {
		Objects.requireNonNull(suffix);
		this.suffix = suffix;
		this.reversedSuffix = new StringBuffer(suffix).reverse().toString();
		return this;
	}

	public String getOSRequirementScript() {
		return hasOSPreScript() ? String.format("source-%s %s", this.getType().toLowerCase(), this.getId()) : null;
	}

	public String getOSEnv() {
		return "";
	}

	public abstract boolean hasOSPreScript();

	@Override
	public final int compareTo(PadlSourceConfiguration o) {
		Objects.requireNonNull(suffix, "All configuration sources must have a suffix.");
		Objects.requireNonNull(o.suffix, "All configuration sources must have a suffix.");
		Objects.requireNonNull(o, "Source configuration object cannot be null");
		return this.reversedSuffix.compareTo(o.reversedSuffix) * -1;
	}

	public PadlSourceFactory getFactory() {
		return factory;
	}

	public void setFactory(PadlSourceFactory factory) {
		this.factory = factory;
	}

	public PadlSourceService getSource() {
		return source;
	}

	public void setSource(PadlSourceService source) {
		this.source = source;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getBaseSuffix() {
		return baseSuffix;
	}

	public void setBaseSuffix(String baseSuffix) {
		this.baseSuffix = baseSuffix;
	}

}
