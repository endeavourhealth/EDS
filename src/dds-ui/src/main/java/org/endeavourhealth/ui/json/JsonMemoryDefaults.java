package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonMemoryDefaults {

    private Integer maxHeap;
    private String unit;

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
