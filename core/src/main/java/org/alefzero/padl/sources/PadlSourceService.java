package org.alefzero.padl.sources;

public abstract class PadlSourceService {

	private PadlSourceConfiguration config;

	private PadlSourceParameters sourceParameters;

	public abstract void sync();

	public PadlSourceConfiguration getConfig() {
		return config;
	}

	public void setConfig(PadlSourceConfiguration config) {
		this.config = config;
	}

	public PadlSourceParameters getSourceParameters() {
		return sourceParameters;
	}

	public void setSourceParameters(PadlSourceParameters sourceParameters) {
		this.sourceParameters = sourceParameters;
	}

}
