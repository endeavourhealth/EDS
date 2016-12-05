package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private Instance instance;
    private List<Port> ports;
    private List<Channel> channels;

    public Instance getInstance() {
        return instance;
    }

    public DbConfiguration setInstance(Instance instance) {
        this.instance = instance;
        return this;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public DbConfiguration setPorts(List<Port> ports) {
        this.ports = ports;
        return this;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public DbConfiguration setChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }
}
