package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformExchangeError {

    private UUID exchangeId = null;
    private Date transformStart = null;
    private Date transformEnd = null;
    private List<String> lines = null;

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public Date getTransformStart() {
        return transformStart;
    }

    public void setTransformStart(Date transformStart) {
        this.transformStart = transformStart;
    }

    public Date getTransformEnd() {
        return transformEnd;
    }

    public void setTransformEnd(Date transformEnd) {
        this.transformEnd = transformEnd;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
