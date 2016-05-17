
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.MessageInterchange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.MessageInterchange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="senderEDICode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="receiverEDICode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="interchange">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="500"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="messageType" type="{http://www.e-mis.com/emisopen}voc.PathMessageType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.MessageInterchange", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "senderEDICode",
    "receiverEDICode",
    "interchange",
    "messageType"
})
public class OpenHR001MessageInterchange {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String senderEDICode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String receiverEDICode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String interchange;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocPathMessageType messageType;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the senderEDICode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderEDICode() {
        return senderEDICode;
    }

    /**
     * Sets the value of the senderEDICode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderEDICode(String value) {
        this.senderEDICode = value;
    }

    /**
     * Gets the value of the receiverEDICode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverEDICode() {
        return receiverEDICode;
    }

    /**
     * Sets the value of the receiverEDICode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverEDICode(String value) {
        this.receiverEDICode = value;
    }

    /**
     * Gets the value of the interchange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterchange() {
        return interchange;
    }

    /**
     * Sets the value of the interchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterchange(String value) {
        this.interchange = value;
    }

    /**
     * Gets the value of the messageType property.
     * 
     * @return
     *     possible object is
     *     {@link VocPathMessageType }
     *     
     */
    public VocPathMessageType getMessageType() {
        return messageType;
    }

    /**
     * Sets the value of the messageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocPathMessageType }
     *     
     */
    public void setMessageType(VocPathMessageType value) {
        this.messageType = value;
    }

}
