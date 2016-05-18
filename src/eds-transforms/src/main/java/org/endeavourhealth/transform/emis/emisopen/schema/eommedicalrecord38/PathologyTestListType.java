
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathologyTestListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathologyTestListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PathologyEvent" type="{http://www.e-mis.com/emisopen/MedicalRecord}PathologyTestType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathologyTestListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "pathologyEvent"
})
public class PathologyTestListType {

    @XmlElement(name = "PathologyEvent", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<PathologyTestType> pathologyEvent;

    /**
     * Gets the value of the pathologyEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pathologyEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPathologyEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PathologyTestType }
     * 
     * 
     */
    public List<PathologyTestType> getPathologyEvent() {
        if (pathologyEvent == null) {
            pathologyEvent = new ArrayList<PathologyTestType>();
        }
        return this.pathologyEvent;
    }

}
