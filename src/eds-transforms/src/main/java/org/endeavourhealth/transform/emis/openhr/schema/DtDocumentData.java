
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.DocumentData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.DocumentData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="reference" type="{http://www.e-mis.com/emisopen}dt.url"/>
 *         &lt;element name="documentData" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="encoding" type="{http://www.e-mis.com/emisopen}voc.BinaryDataEncoding" />
 *       &lt;attribute name="compression" type="{http://www.e-mis.com/emisopen}voc.CompressionAlgorithm" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.DocumentData", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "reference",
    "documentData"
})
public class DtDocumentData {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "anyURI")
    protected String reference;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String documentData;
    @XmlAttribute(name = "encoding")
    protected VocBinaryDataEncoding encoding;
    @XmlAttribute(name = "compression")
    protected VocCompressionAlgorithm compression;

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReference(String value) {
        this.reference = value;
    }

    /**
     * Gets the value of the documentData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentData() {
        return documentData;
    }

    /**
     * Sets the value of the documentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentData(String value) {
        this.documentData = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link VocBinaryDataEncoding }
     *     
     */
    public VocBinaryDataEncoding getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocBinaryDataEncoding }
     *     
     */
    public void setEncoding(VocBinaryDataEncoding value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * @return
     *     possible object is
     *     {@link VocCompressionAlgorithm }
     *     
     */
    public VocCompressionAlgorithm getCompression() {
        return compression;
    }

    /**
     * Sets the value of the compression property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocCompressionAlgorithm }
     *     
     */
    public void setCompression(VocCompressionAlgorithm value) {
        this.compression = value;
    }

}
