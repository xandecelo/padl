package org.alefzero.padl.core.model;

public class LDAPSourceConfig extends PadlSourceConfig {

    public static final String TYPE = "ldap";
    private String host;
    private Integer port;
    private String bindCN;
    private String bindPassword;
    private Boolean useTLS;
    private String dn;

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getBindCN() {
        return bindCN;
    }

    public void setBindCN(String bindCN) {
        this.bindCN = bindCN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public Boolean getUseTLS() {
        return useTLS;
    }

    public void setUseTLS(Boolean useTLS) {
        this.useTLS = useTLS;
    }

    @Override
    public String toString() {
        return "LDAPSourceConfig [bindCN=" + bindCN + ", bindPassword=****, dn=" + dn + ", host=" + host
                + ", port=" + port + ", useTLS=" + useTLS + "]";
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
