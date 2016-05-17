
package org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HolderList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HolderList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Holder" type="{http://www.e-mis.com/emisopen/MedicalRecord}HolderStruct" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HolderList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "holder"
})
public class HolderList {

    @XmlElement(name = "Holder", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected List<HolderStruct> holder;

    /**
     * Gets the value of the holder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the holder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHolder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HolderStruct }
     * 
     * 
     */
    public List<HolderStruct> getHolder() {
        if (holder == null) {
            holder = new ArrayList<HolderStruct>();
        }
        return this.holder;
    }

}
