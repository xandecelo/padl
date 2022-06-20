package org.alefzero.padl.core.model;

/**
 * General configuration
 */
public class PadlConfig {

    private String host;
    private Integer port;
    private String rootDN;
    private String adminPassword;
    private String rootConfigPassword;
    private Boolean useTLS;
    private String type;

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
    public String getRootDN() {
        return rootDN;
    }
    public void setRootDN(String rootDN) {
        this.rootDN = rootDN;
    }
    public String getAdminPassword() {
        return adminPassword;
    }
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
    public String getRootConfigPassword() {
        return rootConfigPassword;
    }
    public void setRootConfigPassword(String rootConfigPassword) {
        this.rootConfigPassword = rootConfigPassword;
    }
    public Boolean getUseTLS() {
        return useTLS;
    }
    public void setUseTLS(Boolean useTLS) {
        this.useTLS = useTLS;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }


    

}
