package org.alefzero.padl.sources;

public interface PadlSourceFactory {

	String getServiceType();

	<T extends PadlSourceConfig> Class<T> getConfigClass();

	<T extends PadlSource> Class<T> getService();

}
