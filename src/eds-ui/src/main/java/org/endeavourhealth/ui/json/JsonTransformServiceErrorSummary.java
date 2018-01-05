package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformServiceErrorSummary {

    private JsonService service = null;
    private UUID systemId = null;
    private String systemName = null;
    private int countExchanges = -1;
    private List<UUID> exchangeIds = null;

    public JsonTransformServiceErrorSummary() {

    }

    public JsonService getService() {
        return service;
    }

    public void setService(JsonService service) {
        this.service = service;
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
}
