
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MixtureItemListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MixtureItemListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MixtureItem" type="{http://www.e-mis.com/emisopen/MedicalRecord}MixtureItemType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MixtureItemListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "mixtureItem"
})
public class MixtureItemListType {

    @XmlElement(name = "MixtureItem", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<MixtureItemType> mixtureItem;

    /**
     * Gets the value of the mixtureItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mixtureItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMixtureItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MixtureItemType }
     * 
     * 
     */
    public List<MixtureItemType> getMixtureItem() {
        if (mixtureItem == null) {
            mixtureItem = new ArrayList<MixtureItemType>();
        }
        return this.mixtureItem;
    }

}
