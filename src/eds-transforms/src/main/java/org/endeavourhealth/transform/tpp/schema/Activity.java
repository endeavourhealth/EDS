
package org.endeavourhealth.transform.tpp.schema;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Activity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Activity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Performed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="With" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Method" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Activity", propOrder = {
    "performed",
    "with",
    "method",
    "duration",
    "linkedProblemUID"
})
public class Activity {

    @XmlElement(name = "Performed", required = true)
    protected String performed;
    @XmlElement(name = "With", required = true)
    protected String with;
    @XmlElement(name = "Method", required = true)
    protected String method;
    @XmlElement(name = "Duration", required = true)
    protected BigInteger duration;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the performed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPerformed() {
        return performed;
    }

    /**
     * Sets the value of the performed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPerformed(String value) {
        this.performed = value;
    }

    /**
     * Gets the value of the with property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWith() {
        return with;
    }

    /**
     * Sets the value of the with property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWith(String value) {
        this.with = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDuration(BigInteger value) {
        this.duration = value;
    }

    /**
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
    }

}
