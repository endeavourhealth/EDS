
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlertType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlertType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="Book" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="AriveSwap" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlertType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "book",
    "ariveSwap"
})
public class AlertType
    extends CodedItemBaseType
{

    @XmlElement(name = "Book", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger book;
    @XmlElement(name = "AriveSwap", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger ariveSwap;

    /**
     * Gets the value of the book property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBook() {
        return book;
    }

    /**
     * Sets the value of the book property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBook(BigInteger value) {
        this.book = value;
    }

    /**
     * Gets the value of the ariveSwap property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAriveSwap() {
        return ariveSwap;
    }

    /**
     * Sets the value of the ariveSwap property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAriveSwap(BigInteger value) {
        this.ariveSwap = value;
    }

}
