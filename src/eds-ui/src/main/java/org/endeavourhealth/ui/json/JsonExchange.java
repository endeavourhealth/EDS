package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonExchange {

    private UUID exchangeId;
    private UUID serviceId;
    private Date timestamp;
    private Map<String, String> headers;
    private List<String> bodyLines;
    private boolean isInError;

    public JsonExchange(UUID exchangeId, UUID serviceId, Date timestamp, Map<String, String> headers, List<String> bodyLines, boolean isInError) {
        this.exchangeId = exchangeId;
        this.serviceId = serviceId;
        this.timestamp = timestamp;
        this.headers = headers;
        this.bodyLines = bodyLines;
        this.isInError = isInError;
    }

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getBodyLines() {
        return bodyLines;
    }

    public void setBodyLines(List<String> bodyLines) {
        this.bodyLines = bodyLines;
    }

    public boolean isInError() {
        return isInError;
    }

    public void setInError(boolean inError) {
        isInError = inError;
    }
}
