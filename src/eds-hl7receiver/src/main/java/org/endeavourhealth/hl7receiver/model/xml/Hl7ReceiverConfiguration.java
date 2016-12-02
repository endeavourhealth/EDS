
package org.endeavourhealth.hl7receiver.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InstanceId" type="{}nonEmptyString"/>
 *         &lt;element name="DatabaseConnections">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SftpReader" type="{}DatabaseConnection"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "instanceId",
    "databaseConnections"
})
@XmlRootElement(name = "Hl7ReceiverConfiguration")
public class Hl7ReceiverConfiguration {

    @XmlElement(name = "InstanceId", required = true)
    protected String instanceId;
    @XmlElement(name = "DatabaseConnections", required = true)
    protected Hl7ReceiverConfiguration.DatabaseConnections databaseConnections;

    /**
     * Gets the value of the instanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the value of the instanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceId(String value) {
        this.instanceId = value;
    }

    /**
     * Gets the value of the databaseConnections property.
     * 
     * @return
     *     possible object is
     *     {@link Hl7ReceiverConfiguration.DatabaseConnections }
     *     
     */
    public Hl7ReceiverConfiguration.DatabaseConnections getDatabaseConnections() {
        return databaseConnections;
    }

    /**
     * Sets the value of the databaseConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link Hl7ReceiverConfiguration.DatabaseConnections }
     *     
     */
    public void setDatabaseConnections(Hl7ReceiverConfiguration.DatabaseConnections value) {
        this.databaseConnections = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="SftpReader" type="{}DatabaseConnection"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "sftpReader"
    })
    public static class DatabaseConnections {

        @XmlElement(name = "SftpReader", required = true)
        protected DatabaseConnection sftpReader;

        /**
         * Gets the value of the sftpReader property.
         * 
         * @return
         *     possible object is
         *     {@link DatabaseConnection }
         *     
         */
        public DatabaseConnection getSftpReader() {
            return sftpReader;
        }

        /**
         * Sets the value of the sftpReader property.
         * 
         * @param value
         *     allowed object is
         *     {@link DatabaseConnection }
         *     
         */
        public void setSftpReader(DatabaseConnection value) {
            this.sftpReader = value;
        }

    }

}
