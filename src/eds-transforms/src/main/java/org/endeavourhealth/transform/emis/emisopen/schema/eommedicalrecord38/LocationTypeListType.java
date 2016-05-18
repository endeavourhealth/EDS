
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LocationTypeListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocationTypeListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LocationType" type="{http://www.e-mis.com/emisopen/MedicalRecord}TypeOfLocationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationTypeListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "locationType"
})
public class LocationTypeListType {

    @XmlElement(name = "LocationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<TypeOfLocationType> locationType;

    /**
     * Gets the value of the locationType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeOfLocationType }
     * 
     * 
     */
    public List<TypeOfLocationType> getLocationType() {
        if (locationType == null) {
            locationType = new ArrayList<TypeOfLocationType>();
        }
        return this.locationType;
    }

}
