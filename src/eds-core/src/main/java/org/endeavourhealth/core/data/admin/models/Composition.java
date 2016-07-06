
package org.endeavourhealth.core.data.admin.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "composition", propOrder = {
    "heading",
    "section"
})
public class Composition {

    @XmlElement(required = true)
    protected String heading;
    protected List<Section> section;

    public String getHeading() {
        return heading;
    }
    public void setHeading(String value) {
        this.heading = value;
    }

    public List<Section> getSection() {
        if (section == null) {
            section = new ArrayList<Section>();
        }
        return this.section;
    }

}







