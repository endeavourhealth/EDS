
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MedicationListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MedicationListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Medication" type="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedicationListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "medication"
})
public class MedicationListType {

    @XmlElement(name = "Medication", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<MedicationType> medication;

    /**
     * Gets the value of the medication property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the medication property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMedication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MedicationType }
     * 
     * 
     */
    public List<MedicationType> getMedication() {
        if (medication == null) {
            medication = new ArrayList<MedicationType>();
        }
        return this.medication;
    }

}
