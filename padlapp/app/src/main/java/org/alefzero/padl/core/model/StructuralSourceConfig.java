package org.alefzero.padl.core.model;

public class StructuralSourceConfig extends PadlSourceConfig {
    
    public static final String TYPE = "structural";

    @Override
    public String toString() {
        return super.toString() + " - structural config";
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
