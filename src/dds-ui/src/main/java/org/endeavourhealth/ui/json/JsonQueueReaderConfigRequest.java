package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueReaderConfigRequest {

    private String configId;
    private Integer maxHeap;
    private String unit;
    private Boolean exclusive;
    private String queue;
    private String pipelineXml;

    public JsonQueueReaderConfigRequest() {
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public Integer getMaxHeap() {
        return maxHeap;
    }

    public void setMaxHeap(Integer maxHeap) {
        this.maxHeap = maxHeap;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getPipelineXml() {
        return pipelineXml;
    }

    public void setPipelineXml(String pipelineXml) {
        this.pipelineXml = pipelineXml;
    }
}
