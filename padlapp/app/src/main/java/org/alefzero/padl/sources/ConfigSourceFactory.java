package org.alefzero.padl.sources;

import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlSourceFactory;

public class ConfigSourceFactory extends PadlSourceFactory {

    @Override
    public PadlSource getInstance() {
        return new ConfigSource();
    }
    
}
