package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by darren on 19/05/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service", propOrder = {
        "uuid",
        "name"
})
public class Service {

    protected String uuid;
    protected String name;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String value) {
        this.uuid = value;
    }

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

}
