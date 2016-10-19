package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformRequeueRequest {

    private UUID serviceId = null;
    private UUID systemId = null;
    private boolean firstExchangeOnly = false;

    public JsonTransformRequeueRequest() {

    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public boolean isFirstExchangeOnly() {
        return firstExchangeOnly;
    }

    public void setFirstExchangeOnly(boolean firstExchangeOnly) {
        this.firstExchangeOnly = firstExchangeOnly;
    }
}
