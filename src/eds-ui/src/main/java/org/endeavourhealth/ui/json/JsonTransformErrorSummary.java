package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformErrorSummary {

    private UUID serviceId = null;
    private String serviceName = null;
    private UUID systemId = null;
    private String systemName = null;
    private int countExchanges = -1;
    private UUID firstExchangeIdInError = null;

    public JsonTransformErrorSummary() {

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

    public UUID getFirstExchangeIdInError() {
        return firstExchangeIdInError;
    }

    public void setFirstExchangeIdInError(UUID firstExchangeIdInError) {
        this.firstExchangeIdInError = firstExchangeIdInError;
    }
}
