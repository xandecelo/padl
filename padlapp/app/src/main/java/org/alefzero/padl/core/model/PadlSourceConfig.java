package org.alefzero.padl.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = LDAPSourceConfig.class, name = LDAPSourceConfig.TYPE),
        @Type(value = DatabaseSourceConfig.class, name = DatabaseSourceConfig.TYPE)
})
public abstract class PadlSourceConfig {

    private String id;
    private String dn;

    public abstract String getType();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    @Override
    public String toString() {
        return "PadlSourceConfig [dn=" + dn + ", id=" + id + ", type=" + getType() + "]";
    }

}
