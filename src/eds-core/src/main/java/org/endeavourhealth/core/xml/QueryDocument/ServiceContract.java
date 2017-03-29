
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceContract complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="serviceContract">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{}serviceContractType"/>
 *         &lt;element name="service" type="{}service"/>
 *         &lt;element name="system" type="{}system"/>
 *         &lt;element name="technicalInterface" type="{}technicalInterface"/>
 *         &lt;element name="active" type="{}serviceContractActive"/>
 *         &lt;element name="definesCohort" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceContract", propOrder = {
    "type",
    "service",
    "system",
    "technicalInterface",
    "active",
    "definesCohort"
})
public class ServiceContract {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ServiceContractType type;
    @XmlElement(required = true)
    protected Service service;
    @XmlElement(required = true)
    protected System system;
    @XmlElement(required = true)
    protected TechnicalInterface technicalInterface;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ServiceContractActive active;
    protected Boolean definesCohort;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceContractType }
     *     
     */
    public ServiceContractType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceContractType }
     *     
     */
    public void setType(ServiceContractType value) {
        this.type = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link Service }
     *     
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link Service }
     *     
     */
    public void setService(Service value) {
        this.service = value;
    }

    /**
     * Gets the value of the system property.
     * 
     * @return
     *     possible object is
     *     {@link System }
     *     
     */
    public System getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     * 
     * @param value
     *     allowed object is
     *     {@link System }
     *     
     */
    public void setSystem(System value) {
        this.system = value;
    }

    /**
     * Gets the value of the technicalInterface property.
     * 
     * @return
     *     possible object is
     *     {@link TechnicalInterface }
     *     
     */
    public TechnicalInterface getTechnicalInterface() {
        return technicalInterface;
    }

    /**
     * Sets the value of the technicalInterface property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechnicalInterface }
     *     
     */
    public void setTechnicalInterface(TechnicalInterface value) {
        this.technicalInterface = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceContractActive }
     *     
     */
    public ServiceContractActive getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceContractActive }
     *     
     */
    public void setActive(ServiceContractActive value) {
        this.active = value;
    }

    /**
     * Gets the value of the definesCohort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDefinesCohort() {
        return definesCohort;
    }

    /**
     * Sets the value of the definesCohort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefinesCohort(Boolean value) {
        this.definesCohort = value;
    }

}
