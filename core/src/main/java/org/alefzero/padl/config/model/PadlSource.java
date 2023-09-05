package org.alefzero.padl.config.model;

public interface PadlSource {
	
	public void prepare();
	
	public void sync();
	
	public void end();
	
}
