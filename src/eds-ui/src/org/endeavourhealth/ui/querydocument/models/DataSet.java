
package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataSet", propOrder = {
    "composition"
})
public class DataSet {

    @XmlElement(required = true)
    protected List<Composition> composition;

    public List<Composition> getComposition() {
        if (composition == null) {
            composition = new ArrayList<Composition>();
        }
        return this.composition;
    }

}
