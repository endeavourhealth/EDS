package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonPostToExchangeRequest {

    private UUID exchangeId;
    private UUID serviceId;
    private UUID systemId;
    private String exchangeName;
    private String postMode;
    private Set<String> specificSubscriberConfigNames;
    private String fileTypesToFilterOn;
    private Boolean deleteTransformErrorState;
    private String reason; //why we're posting
    private Map<String, String> additionalHeaders; //anything extra to be added to the exchange

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

    public Set<String> getSpecificSubscriberConfigNames() {
        return specificSubscriberConfigNames;
    }

    public void setSpecificSubscriberConfigNames(Set<String> specificSubscriberConfigNames) {
        this.specificSubscriberConfigNames = specificSubscriberConfigNames;
    }

    public String getFileTypesToFilterOn() {
        return fileTypesToFilterOn;
    }

    public void setFileTypesToFilterOn(String fileTypesToFilterOn) {
        this.fileTypesToFilterOn = fileTypesToFilterOn;
    }

    public Boolean getDeleteTransformErrorState() {
        return deleteTransformErrorState;
    }

    public void setDeleteTransformErrorState(Boolean deleteTransformErrorState) {
        this.deleteTransformErrorState = deleteTransformErrorState;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    public void setAdditionalHeaders(Map<String, String> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }
}
