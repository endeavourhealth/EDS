package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darren on 19/05/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "system", propOrder = {
        "uuid",
        "name",
        "technicalInterface"
})
public class System {

    protected String uuid;
    protected String name;
    protected List<TechnicalInterface> technicalInterface;

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

    public List<TechnicalInterface> getTechnicalInterface() {
        if (technicalInterface == null) {
            technicalInterface = new ArrayList<TechnicalInterface>();
        }
        return this.technicalInterface;
    }
}
