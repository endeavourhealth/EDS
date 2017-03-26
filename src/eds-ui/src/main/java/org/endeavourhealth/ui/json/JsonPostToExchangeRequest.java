package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonPostToExchangeRequest {

    private UUID exchangeId;
    private UUID serviceId;
    private String exchangeName;
    private boolean postAllExchanges;

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

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public boolean isPostAllExchanges() {
        return postAllExchanges;
    }

    public void setPostAllExchanges(boolean postAllExchanges) {
        this.postAllExchanges = postAllExchanges;
    }
}
