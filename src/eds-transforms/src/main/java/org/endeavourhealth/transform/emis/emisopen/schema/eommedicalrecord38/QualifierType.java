
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * INFORMATION
 * The Qualifier ITEM id is unique for the system but are devided into groups to create a quailifier hierarchy)
 * Known Group are 1= Family member, 2 = trend, 3 = laterality, 4= Severity, 5= Numerical Value, 6= Causatiion, 7 = GMS 8=Certainty
 * The group ID may not be included in the message and the group and term attribute is populated for reference only
 * 
 * <p>Java class for QualifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QualifierItemID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *         &lt;element name="Group" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualifierType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "qualifierItemID",
    "group"
})
public class QualifierType {

    @XmlElement(name = "QualifierItemID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType qualifierItemID;
    @XmlElement(name = "Group", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType group;

    /**
     * Gets the value of the qualifierItemID property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerCodeType }
     *     
     */
    public IntegerCodeType getQualifierItemID() {
        return qualifierItemID;
    }

    /**
     * Sets the value of the qualifierItemID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerCodeType }
     *     
     */
    public void setQualifierItemID(IntegerCodeType value) {
        this.qualifierItemID = value;
    }

    /**
     * Gets the value of the group property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerCodeType }
     *     
     */
    public IntegerCodeType getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerCodeType }
     *     
     */
    public void setGroup(IntegerCodeType value) {
        this.group = value;
    }

}
