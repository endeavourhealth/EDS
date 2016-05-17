
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SlotTypeList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SlotTypeList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SlotType" type="{http://www.e-mis.com/emisopen/MedicalRecord}SlotsStruct" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SlotTypeList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "slotType"
})
public class SlotTypeList {

    @XmlElement(name = "SlotType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected List<SlotsStruct> slotType;

    /**
     * Gets the value of the slotType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the slotType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSlotType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SlotsStruct }
     * 
     * 
     */
    public List<SlotsStruct> getSlotType() {
        if (slotType == null) {
            slotType = new ArrayList<SlotsStruct>();
        }
        return this.slotType;
    }

}
