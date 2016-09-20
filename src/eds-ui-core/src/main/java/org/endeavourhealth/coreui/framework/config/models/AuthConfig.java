package org.endeavourhealth.coreui.framework.config.models;

public class AuthConfig {

    private String realm;
    private String authServerUrl;
    private String authClientId;
    private String appUrl;

    public AuthConfig(String realm, String authServerUrl, String authClientId, String appUrl) {
        this.realm = realm;
        this.authServerUrl = authServerUrl;
        this.authClientId = authClientId;
        this.appUrl = appUrl;
    }

    public String getRealm() {

        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getAuthClientId() {
        return authClientId;
    }

    public void setAuthClientId(String authClientId) {
        this.authClientId = authClientId;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }
}
