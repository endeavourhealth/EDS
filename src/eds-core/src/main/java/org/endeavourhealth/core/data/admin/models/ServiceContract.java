package org.endeavourhealth.core.data.admin.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.lang.*;

/**
 * Created by darren on 19/05/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceContract", propOrder = {
        "type",
        "service",
        "system",
        "technicalInterface",
        "active"
})
public class ServiceContract {

    protected String type;
    protected org.endeavourhealth.core.data.config.models.Service service;
    protected System system;
    protected TechnicalInterface technicalInterface;
    protected String active;

    public String getType() {
        return type;
    }
    public void setType(String value) {
        this.type = value;
    }

    public org.endeavourhealth.core.data.config.models.Service getService() {
        return service;
    }
    public void setService(org.endeavourhealth.core.data.config.models.Service value) {
        this.service = value;
    }

    public System getSystem() {
        return system;
    }
    public void setSystem(System value) {
        this.system = value;
    }

    public TechnicalInterface getTechnicalInterface() {
        return technicalInterface;
    }
    public void setTechnicalInterface(TechnicalInterface value) {
        this.technicalInterface = value;
    }

    public String getActive() {
        return active;
    }
    public void setActive(String value) {
        this.active = value;
    }

}
