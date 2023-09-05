package org.alefzero.padl.sources;

public abstract class PadlSourceFactory {

	public abstract String getServiceType();

	public abstract <T extends PadlSourceFactorySetup> Class<T> getSourceFactorySetupClass();

	public abstract void setFactorySetup(PadlSourceFactorySetup factorySetup);

	public abstract <T extends PadlSourceFactorySetup> T getFactorySetup();

	public abstract <T extends PadlSourceServiceConfig> Class<T> getServiceConfigType();

	public abstract <T extends PadlSourceService> Class<T> getService();

}
