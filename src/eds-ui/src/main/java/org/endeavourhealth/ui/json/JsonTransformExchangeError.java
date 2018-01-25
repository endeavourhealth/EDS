package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTransformExchangeError {

    private UUID exchangeId = null;
    private UUID version = null;
    private String eventDesc = null;
    private Date transformStart = null;
    private Date transformEnd = null;
    private Integer numberBatchIdsCreated = null;
    private boolean hadErrors;
    private boolean resubmitted;
    private Date deleted = null;
    private List<String> lines = null;

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
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

    public Integer getNumberBatchIdsCreated() {
        return numberBatchIdsCreated;
    }

    public void setNumberBatchIdsCreated(Integer numberBatchIdsCreated) {
        this.numberBatchIdsCreated = numberBatchIdsCreated;
    }

    public boolean isHadErrors() {
        return hadErrors;
    }

    public void setHadErrors(boolean hadErrors) {
        this.hadErrors = hadErrors;
    }

    public boolean isResubmitted() {
        return resubmitted;
    }

    public void setResubmitted(boolean resubmitted) {
        this.resubmitted = resubmitted;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
