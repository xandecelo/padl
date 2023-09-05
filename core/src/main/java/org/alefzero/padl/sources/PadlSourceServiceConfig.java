package org.alefzero.padl.sources;

import java.util.Objects;

public abstract class PadlSourceServiceConfig implements Comparable<PadlSourceServiceConfig> {

	private PadlSourceFactory factory = null;
	
	public abstract String getConfigurationLDIF();
	
	private String id;
	private String type;
	private String suffix;
	private String reversedSuffix;
	private String rootDN;
	private Boolean enabled = true;

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

	public PadlSourceServiceConfig setSuffix(String suffix) {
		Objects.requireNonNull(suffix);
		this.suffix = suffix;
		this.reversedSuffix = new StringBuffer(suffix).reverse().toString();
		return this;
	}
	
	public String getOSRequirementScript() {
		return hasOSPrerequirementScript() ? "source-" + this.getType().toLowerCase() : null;
	}

	public String getOSEnv() {
		return "";
	}

	protected abstract boolean hasOSPrerequirementScript();

	@Override
	public final int compareTo(PadlSourceServiceConfig o) {
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

}
