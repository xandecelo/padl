package org.alefzero.padl.core.model;

public class DatabaseSourceConfig extends PadlSourceConfig {
    
    public static final String TYPE = "database";

    @Override
    public String toString() {
        return super.toString() + " - database config";
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
