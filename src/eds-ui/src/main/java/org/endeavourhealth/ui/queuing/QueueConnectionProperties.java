package org.endeavourhealth.ui.queuing;

public class QueueConnectionProperties {
    private final String ipAddress;
    private final String username;
    private final String password;

    public QueueConnectionProperties(String ipAddress, String username, String password) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
