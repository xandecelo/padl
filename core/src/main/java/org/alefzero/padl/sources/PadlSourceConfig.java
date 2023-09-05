package org.alefzero.padl.sources;

import java.util.Objects;

public abstract class PadlSourceConfig implements Comparable<PadlSourceConfig> {

	public abstract String getConfigurationLDIF();

	private String id;
	private String type;
	private String suffix;
	private String reversedSuffix;
	private String rootDN;

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

	public PadlSourceConfig setSuffix(String suffix) {
		Objects.requireNonNull(suffix);
		this.suffix = suffix;
		this.reversedSuffix = new StringBuffer(suffix).reverse().toString();
		return this;
	}

	@Override
	public final int compareTo(PadlSourceConfig o) {
		Objects.requireNonNull(suffix, "All configuration sources must have a suffix.");
		Objects.requireNonNull(o.suffix, "All configuration sources must have a suffix.");
		Objects.requireNonNull(o, "Source configuration object cannot be null");
		return this.reversedSuffix.compareTo(o.reversedSuffix) * -1;
	}

}
