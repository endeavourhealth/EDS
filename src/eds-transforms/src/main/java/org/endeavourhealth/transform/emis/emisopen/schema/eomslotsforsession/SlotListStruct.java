
package org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SlotListStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SlotListStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Slot" type="{}SlotStruct" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SlotListStruct", propOrder = {
    "slot"
})
public class SlotListStruct {

    @XmlElement(name = "Slot")
    protected List<SlotStruct> slot;

    /**
     * Gets the value of the slot property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the slot property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSlot().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SlotStruct }
     * 
     * 
     */
    public List<SlotStruct> getSlot() {
        if (slot == null) {
            slot = new ArrayList<SlotStruct>();
        }
        return this.slot;
    }

}
