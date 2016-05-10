
package org.endeavourhealth.core.transform.tpp.schema;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Drug complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Drug">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="Scheme" use="required" type="{}DrugScheme" />
 *       &lt;attribute name="ProductID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="PackID" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Drug")
public class Drug {

    @XmlAttribute(name = "Scheme", required = true)
    protected DrugScheme scheme;
    @XmlAttribute(name = "ProductID", required = true)
    protected String productID;
    @XmlAttribute(name = "PackID")
    protected BigInteger packID;

    /**
     * Gets the value of the scheme property.
     * 
     * @return
     *     possible object is
     *     {@link DrugScheme }
     *     
     */
    public DrugScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the value of the scheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link DrugScheme }
     *     
     */
    public void setScheme(DrugScheme value) {
        this.scheme = value;
    }

    /**
     * Gets the value of the productID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductID() {
        return productID;
    }

    /**
     * Sets the value of the productID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductID(String value) {
        this.productID = value;
    }

    /**
     * Gets the value of the packID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPackID() {
        return packID;
    }

    /**
     * Sets the value of the packID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPackID(BigInteger value) {
        this.packID = value;
    }

}
