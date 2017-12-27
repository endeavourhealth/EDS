package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformServiceErrorSummary {

    private UUID serviceId = null;
    private String serviceName = null;
    private String servicePublisherConfigName = null;
    private UUID systemId = null;
    private String systemName = null;
    private int countExchanges = -1;
    private List<UUID> exchangeIds = null;
    private String serviceLocalIdentifier = null;

    public JsonTransformServiceErrorSummary() {

    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePublisherConfigName() {
        return servicePublisherConfigName;
    }

    public void setServicePublisherConfigName(String servicePublisherConfigName) {
        this.servicePublisherConfigName = servicePublisherConfigName;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public int getCountExchanges() {
        return countExchanges;
    }

    public void setCountExchanges(int countExchanges) {
        this.countExchanges = countExchanges;
    }

    public List<UUID> getExchangeIds() {
        return exchangeIds;
    }

    public void setExchangeIds(List<UUID> exchangeIds) {
        this.exchangeIds = exchangeIds;
    }

    public String getServiceLocalIdentifier() {
        return serviceLocalIdentifier;
    }

    public void setServiceLocalIdentifier(String serviceLocalIdentifier) {
        this.serviceLocalIdentifier = serviceLocalIdentifier;
    }
}
