
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AllergyListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AllergyListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Allergy" type="{http://www.e-mis.com/emisopen/MedicalRecord}AllergyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllergyListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "allergy"
})
public class AllergyListType {

    @XmlElement(name = "Allergy", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<AllergyType> allergy;

    /**
     * Gets the value of the allergy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allergy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllergy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AllergyType }
     * 
     * 
     */
    public List<AllergyType> getAllergy() {
        if (allergy == null) {
            allergy = new ArrayList<AllergyType>();
        }
        return this.allergy;
    }

}
