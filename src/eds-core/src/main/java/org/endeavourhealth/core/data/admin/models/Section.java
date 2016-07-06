
package org.endeavourhealth.core.data.admin.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "section", propOrder = {
    "heading",
    "resource"
})
public class Section {

    @XmlElement(required = true)
    protected String heading;
    protected List<Resource> resource;

    public String getHeading() {
        return heading;
    }
    public void setHeading(String value) {
        this.heading = value;
    }

    public List<Resource> getResource() {
        if (resource == null) {
            resource = new ArrayList<Resource>();
        }
        return this.resource;
    }

}







