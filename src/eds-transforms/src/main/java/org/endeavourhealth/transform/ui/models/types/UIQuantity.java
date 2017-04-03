package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIQuantity {
    private Double value;
    private String units;
    private String comparator;

    public Double getValue() {
        return value;
    }

    public UIQuantity setValue(Double value) {
        this.value = value;
        return this;
    }

    public String getUnits() {
        return units;
    }

    public UIQuantity setUnits(String units) {
        this.units = units;
        return this;
    }

    public String getComparator() {
        return comparator;
    }

    public UIQuantity setComparator(String comparator) {
        this.comparator = comparator;
        return this;
    }
}
