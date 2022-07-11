package org.alefzero.padl.core.model;

public class ConfigSourceConfig extends PadlSourceConfig {

    public static final String TYPE = "config";

    public String attributeType;
    public String ldif;

    @Override
    public String getType() {
        return TYPE;
    }

    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getLdif() {
        return ldif;
    }

    public void setLdif(String ldif) {
        this.ldif = ldif;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public String toString() {
        return "ConfigSourceConfig [attributeType=" + attributeType + ", config=" + config + ", ldif=" + ldif + "]";
    }

}
