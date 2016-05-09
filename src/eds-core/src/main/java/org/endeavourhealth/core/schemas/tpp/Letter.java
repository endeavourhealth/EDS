
package org.endeavourhealth.core.schemas.tpp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Letter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Letter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RecipientName" type="{}LetterPerson" minOccurs="0"/>
 *         &lt;element name="RecipientOrganisation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RecipientAddress" type="{}Address"/>
 *         &lt;element name="SenderName" type="{}LetterPerson" minOccurs="0"/>
 *         &lt;element name="SenderOrganisation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SenderAddress" type="{}Address"/>
 *         &lt;element name="DocumentUid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Direction">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Incoming"/>
 *               &lt;enumeration value="Outgoing"/>
 *               &lt;enumeration value="Other"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DateSentReceived" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
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
@XmlType(name = "Letter", propOrder = {
    "recipientName",
    "recipientOrganisation",
    "recipientAddress",
    "senderName",
    "senderOrganisation",
    "senderAddress",
    "documentUid",
    "type",
    "direction",
    "dateSentReceived",
    "linkedProblemUID"
})
public class Letter {

    @XmlElement(name = "RecipientName")
    protected LetterPerson recipientName;
    @XmlElement(name = "RecipientOrganisation")
    protected String recipientOrganisation;
    @XmlElement(name = "RecipientAddress", required = true)
    protected Address recipientAddress;
    @XmlElement(name = "SenderName")
    protected LetterPerson senderName;
    @XmlElement(name = "SenderOrganisation")
    protected String senderOrganisation;
    @XmlElement(name = "SenderAddress", required = true)
    protected Address senderAddress;
    @XmlElement(name = "DocumentUid", required = true)
    protected String documentUid;
    @XmlElement(name = "Type", required = true)
    protected String type;
    @XmlElement(name = "Direction", required = true)
    protected String direction;
    @XmlElement(name = "DateSentReceived")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateSentReceived;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the recipientName property.
     * 
     * @return
     *     possible object is
     *     {@link LetterPerson }
     *     
     */
    public LetterPerson getRecipientName() {
        return recipientName;
    }

    /**
     * Sets the value of the recipientName property.
     * 
     * @param value
     *     allowed object is
     *     {@link LetterPerson }
     *     
     */
    public void setRecipientName(LetterPerson value) {
        this.recipientName = value;
    }

    /**
     * Gets the value of the recipientOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipientOrganisation() {
        return recipientOrganisation;
    }

    /**
     * Sets the value of the recipientOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipientOrganisation(String value) {
        this.recipientOrganisation = value;
    }

    /**
     * Gets the value of the recipientAddress property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getRecipientAddress() {
        return recipientAddress;
    }

    /**
     * Sets the value of the recipientAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setRecipientAddress(Address value) {
        this.recipientAddress = value;
    }

    /**
     * Gets the value of the senderName property.
     * 
     * @return
     *     possible object is
     *     {@link LetterPerson }
     *     
     */
    public LetterPerson getSenderName() {
        return senderName;
    }

    /**
     * Sets the value of the senderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link LetterPerson }
     *     
     */
    public void setSenderName(LetterPerson value) {
        this.senderName = value;
    }

    /**
     * Gets the value of the senderOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderOrganisation() {
        return senderOrganisation;
    }

    /**
     * Sets the value of the senderOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderOrganisation(String value) {
        this.senderOrganisation = value;
    }

    /**
     * Gets the value of the senderAddress property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getSenderAddress() {
        return senderAddress;
    }

    /**
     * Sets the value of the senderAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setSenderAddress(Address value) {
        this.senderAddress = value;
    }

    /**
     * Gets the value of the documentUid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentUid() {
        return documentUid;
    }

    /**
     * Sets the value of the documentUid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentUid(String value) {
        this.documentUid = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirection(String value) {
        this.direction = value;
    }

    /**
     * Gets the value of the dateSentReceived property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateSentReceived() {
        return dateSentReceived;
    }

    /**
     * Sets the value of the dateSentReceived property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateSentReceived(XMLGregorianCalendar value) {
        this.dateSentReceived = value;
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
