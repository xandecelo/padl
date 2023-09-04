package org.alefzero.padl.config.model;

public interface PadlSourceFactory {

	String getServiceType();

	<T extends PadlSourceConfig> Class<T> getConfigClass();

	<T extends PadlSource> Class<T> getService();

}
