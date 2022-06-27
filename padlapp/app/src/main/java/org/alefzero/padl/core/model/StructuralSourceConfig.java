package org.alefzero.padl.core.model;

import java.util.Set;

public class StructuralSourceConfig extends PadlSourceConfig {

    public static final String TYPE = "structural";

    @Override
    public String getType() {
        return TYPE;
    }

    private String ldapType;
    private String value;
    private Set<String> attributes;
    private Set<String> objectClasses;

    public String getLdapType() {
        return ldapType;
    }

    public void setLdapType(String ldapType) {
        this.ldapType = ldapType;
    }

    public Set<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Set<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
