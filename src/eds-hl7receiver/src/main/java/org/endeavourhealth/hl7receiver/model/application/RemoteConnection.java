package org.endeavourhealth.hl7receiver.model.application;

import ca.uhn.hl7v2.app.Connection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RemoteConnection {
    private Connection connection;
    private String host;
    private int port;

    public RemoteConnection(Connection connection) {
        this.connection = connection;
        this.host = connection.getRemoteAddress().getHostAddress();
        this.port = connection.getRemotePort();
    }

    public RemoteConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(host)
                .append(port)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteConnection))
            return false;

        if (obj == this)
            return true;

        RemoteConnection rhs = (RemoteConnection)obj;

        return new EqualsBuilder()
                .append(host, rhs.host)
                .append(port, rhs.port)
                .isEquals();
    }
}
