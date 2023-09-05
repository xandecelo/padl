package org.alefzero.padl.sources;

public abstract class PadlSourceFactory {

	public abstract String getServiceType();
	
	public abstract <T extends PadlSourceParameters> Class<T> getSourceParameterClassType();

	public abstract <T extends PadlSourceConfiguration> Class<T> getSourceConfigType();
	
	public abstract void setSourceParameters(PadlSourceParameters sourceParameters);

	public abstract <T extends PadlSourceParameters> T getSourceParameters();

	public abstract <T extends PadlSourceService> T getService();


}
