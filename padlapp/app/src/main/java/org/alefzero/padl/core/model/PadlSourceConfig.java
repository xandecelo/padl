package org.alefzero.padl.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo (
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type"
)
@JsonSubTypes({ 
    @Type(value = LDAPSourceConfig.class, name = "ldap"), 
    @Type(value = DatabaseSourceConfig.class, name = "database") 
  })
public abstract class PadlSourceConfig {

    private String id;
    private String type;
    private String dn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    @Override
    public String toString() {
        return "PadlSourceConfig [dn=" + dn + ", id=" + id + ", type=" + type + "]";
    }

}
