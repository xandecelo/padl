package org.alefzero.padl.sources;

public interface PadlSourceService {
	
	public <T extends PadlSourceServiceConfig> T setConfig(T config);
	
	public <T extends PadlSourceFactorySetup> T setFactory(T setup);
	
	public void prepare();
	
	public void sync();
	
	public void end();
	
}
