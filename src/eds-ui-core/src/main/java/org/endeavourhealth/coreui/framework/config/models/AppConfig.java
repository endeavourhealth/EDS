package org.endeavourhealth.coreui.framework.config.models;

public class AppConfig {

    private String appUrl;

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public AppConfig() {
    }

    public AppConfig(String appUrl) {
        this.appUrl = appUrl;
    }
}
