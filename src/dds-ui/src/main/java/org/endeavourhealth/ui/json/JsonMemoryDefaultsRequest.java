package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonMemoryDefaultsRequest {

    private String queueType;
    private Integer maxHeap;
    private String unit;

    public JsonMemoryDefaultsRequest() {
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
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
}
