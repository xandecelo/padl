package org.alefzero.padl.sources;

public abstract class PadlSourceParameters {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOSEnv() {
		return "";
	}

}
