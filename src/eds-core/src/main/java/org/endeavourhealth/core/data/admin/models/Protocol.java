package org.endeavourhealth.core.data.admin.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darren on 19/05/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "protocol", propOrder = {
        "enabled",
        "patientConsent",
        "cohort",
        "dataSet",
        "serviceContract"
})
public class Protocol {

    protected String enabled;
    protected String patientConsent;
    protected String cohort;
    protected String dataSet;
    protected List<ServiceContract> serviceContract;

    public String getEnabled() {
        return enabled;
    }
    public void setEnabled(String value) {
        this.enabled = value;
    }

    public String getPatientConsent() {
        return patientConsent;
    }
    public void setPatientConsent(String value) {
        this.patientConsent = value;
    }

    public String getCohort() {
        return cohort;
    }
    public void setCohort(String value) {
        this.cohort = value;
    }

    public String getDataSet() {
        return dataSet;
    }
    public void setDataSet(String value) {
        this.dataSet = value;
    }

    public List<ServiceContract> getServiceContract() {
        if (serviceContract == null) {
            serviceContract = new ArrayList<ServiceContract>();
        }
        return this.serviceContract;
    }


}
