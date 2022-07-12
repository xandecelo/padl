package org.alefzero.padl.core.model;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = LDAPSourceConfig.class, name = LDAPSourceConfig.TYPE),
        @Type(value = DatabaseSourceConfig.class, name = DatabaseSourceConfig.TYPE),
        @Type(value = StructuralSourceConfig.class, name = StructuralSourceConfig.TYPE),
        @Type(value = ConfigSourceConfig.class, name = ConfigSourceConfig.TYPE)
})
public abstract class PadlSourceConfig {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String LOAD_STRATEGY_MERGE = "merge";
    public static final String LOAD_STRATEGY_REPLACE = "replace";
    public static final String LOAD_STRATEGY_IGNORE = "ignore";
    // TODO: fix lowercase/uppercase misshandle
    public static final String LOAD_STRATEGY_ADD_ATTRIBUTE = "addattribute";

    private String id;
    private String dn;
    private String loadStrategy;

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

    public String getLoadStrategy() {
        loadStrategy = loadStrategy != null ? loadStrategy.toLowerCase() : LOAD_STRATEGY_MERGE;
        return loadStrategy;
    }

    public void setLoadStrategy(String loadStrategy) {
        this.loadStrategy = loadStrategy != null ? loadStrategy.toLowerCase() : LOAD_STRATEGY_MERGE;
        switch (this.loadStrategy) {
            case LOAD_STRATEGY_IGNORE:
            case LOAD_STRATEGY_MERGE:
            case LOAD_STRATEGY_REPLACE:
            case LOAD_STRATEGY_ADD_ATTRIBUTE:
                break;
            default:
                logger.warn("Source load strategy [{}] is invalid. Using loadStrategy=merge as default.");
                this.loadStrategy = LOAD_STRATEGY_MERGE;
        }
    }

    @Override
    public String toString() {
        return "PadlSourceConfig [dn=" + dn + ", id=" + id + ", loadStrategy=" + loadStrategy + "]";
    }

}
