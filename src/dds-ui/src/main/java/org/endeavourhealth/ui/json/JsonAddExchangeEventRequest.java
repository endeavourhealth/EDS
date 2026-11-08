package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonAddExchangeEventRequest {

    private UUID exchangeId;
    private String text;

    public JsonAddExchangeEventRequest() {
    }

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
