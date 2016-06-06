package org.endeavourhealth.transform.emis.csv.transforms.coding;

public class DrugCode {

    private String drugName = null;
    private Long dmdId = null;

    public DrugCode(String drugName, Long dmdId) {
        this.drugName = drugName;
        this.dmdId = dmdId;
    }

    public String getDrugName() {
        return drugName;
    }

    public Long getDmdId() {
        return dmdId;
    }
}
