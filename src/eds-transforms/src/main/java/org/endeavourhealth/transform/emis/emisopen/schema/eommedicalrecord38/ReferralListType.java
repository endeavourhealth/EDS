
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReferralListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferralListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Referral" type="{http://www.e-mis.com/emisopen/MedicalRecord}ReferralType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferralListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "referral"
})
public class ReferralListType {

    @XmlElement(name = "Referral", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<ReferralType> referral;

    /**
     * Gets the value of the referral property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referral property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferral().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferralType }
     * 
     * 
     */
    public List<ReferralType> getReferral() {
        if (referral == null) {
            referral = new ArrayList<ReferralType>();
        }
        return this.referral;
    }

}
