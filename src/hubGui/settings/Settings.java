/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.settings;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcin
 */
@XmlRootElement( name = "settings")
public class Settings {
    
    private ArrayList<HubConfig> hubConfigs = new ArrayList<>();
    private String restUrl;
    private String lang;
    private Boolean useProxy;
    private String proxyHost;
    private String proxyPort;
    private String proxyUser;
    private String proxyPassword;

    /**
     * @return the hubConfigs
     */
    public ArrayList<HubConfig> getHubConfigs() {
        return hubConfigs;
    }

    /**
     * @param hubConfigs the hubConfigs to set
     */
    public void setHubConfigs(ArrayList<HubConfig> hubConfigs) {
        this.hubConfigs = hubConfigs;
    }

    /**
     * @return the restUrl
     */
    public String getRestUrl() {
        return restUrl;
    }

    /**
     * @param restUrl the restUrl to set
     */
    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    /**
     * @return the locale
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param locale the locale to set
     */
    public void setLang(String locale) {
        this.lang = locale;
    }

    /**
     * @return the useProxy
     */
    public Boolean isUseProxy() {
        return useProxy;
    }

    /**
     * @param useProxy the useProxy to set
     */
    public void setUseProxy(Boolean useProxy) {
        this.useProxy = useProxy;
    }

    /**
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * @return the proxyPort
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @return the proxyUser
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * @param proxyUser the proxyUser to set
     */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /**
     * @return the proxyPassword
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * @param proxyPassword the proxyPassword to set
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}
