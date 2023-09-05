package org.alefzero.padl.sources;

public interface PadlSource {
	
	public void prepare();
	
	public void sync();
	
	public void end();
	
}
