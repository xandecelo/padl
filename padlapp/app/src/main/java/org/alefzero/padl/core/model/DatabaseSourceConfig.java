package org.alefzero.padl.core.model;

import java.util.Set;

public class DatabaseSourceConfig extends PadlSourceConfig {

    public static final String TYPE = "database";

    @Override
    public String getType() {
        return TYPE;
    }

    private String jdbcUrl;
    private String dbUsername;
    private String dbPassword;
    private String query;
    private String uid;
    private String ldapType;
    private Set<String> datamap;
    private Set<String> objectClasses;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Set<String> getDatamap() {
        return datamap;
    }

    public void setDatamap(Set<String> datamap) {
        this.datamap = datamap;
    }

    public Set<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Set<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLdapType() {
        return ldapType;
    }

    public void setLdapType(String ldapType) {
        this.ldapType = ldapType;
    }

    @Override
    public String toString() {
        return "DatabaseSourceConfig [datamap=" + datamap + ", dbPassword=*****, dbUsername=" + dbUsername
                + ", jdbcUrl=" + jdbcUrl + ", objectClasses=" + objectClasses + ", query=" + query + "]";
    }

}
