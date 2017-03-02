
package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for codeSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="codeSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codingSystem" type="{}codingSystem"/>
 *         &lt;element name="codeSetValue" type="{}codeSetValue" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeSet", propOrder = {
    "codingSystem",
    "codeSetValue"
})
public class CodeSet {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected CodingSystem codingSystem;
    @XmlElement(required = true)
    protected List<CodeSetValue> codeSetValue;

    /**
     * Gets the value of the codingSystem property.
     * 
     * @return
     *     possible object is
     *     {@link CodingSystem }
     *     
     */
    public CodingSystem getCodingSystem() {
        return codingSystem;
    }

    /**
     * Sets the value of the codingSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodingSystem }
     *     
     */
    public void setCodingSystem(CodingSystem value) {
        this.codingSystem = value;
    }

    /**
     * Gets the value of the codeSetValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSetValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSetValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSetValue }
     * 
     * 
     */
    public List<CodeSetValue> getCodeSetValue() {
        if (codeSetValue == null) {
            codeSetValue = new ArrayList<CodeSetValue>();
        }
        return this.codeSetValue;
    }

}
