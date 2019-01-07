package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonPostToExchangeRequest {

    private UUID exchangeId;
    private UUID serviceId;
    private UUID systemId;
    private String exchangeName;
    private String postMode;
    private UUID specificProtocolId;
    private String fileTypesToFilterOn;

    public JsonPostToExchangeRequest() {}

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
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

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getPostMode() {
        return postMode;
    }

    public void setPostMode(String postMode) {
        this.postMode = postMode;
    }

    public UUID getSpecificProtocolId() {
        return specificProtocolId;
    }

    public void setSpecificProtocolId(UUID specificProtocolId) {
        this.specificProtocolId = specificProtocolId;
    }

    public String getFileTypesToFilterOn() {
        return fileTypesToFilterOn;
    }

    public void setFileTypesToFilterOn(String fileTypesToFilterOn) {
        this.fileTypesToFilterOn = fileTypesToFilterOn;
    }
}
