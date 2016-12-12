
package org.endeavourhealth.utilities.configuration.model;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="InstanceName" type="{}NonEmptyString"/>
 *         &lt;element name="DatabaseConnections">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="DatabaseConnection" type="{}DatabaseConnection" maxOccurs="2"/>
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
    "instanceName",
    "databaseConnections"
})
@XmlRootElement(name = "LocalConfiguration")
public class LocalConfiguration {

    @XmlElement(name = "InstanceName", required = true)
    protected String instanceName;
    @XmlElement(name = "DatabaseConnections", required = true)
    protected LocalConfiguration.DatabaseConnections databaseConnections;

    /**
     * Gets the value of the instanceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Sets the value of the instanceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceName(String value) {
        this.instanceName = value;
    }

    /**
     * Gets the value of the databaseConnections property.
     * 
     * @return
     *     possible object is
     *     {@link LocalConfiguration.DatabaseConnections }
     *     
     */
    public LocalConfiguration.DatabaseConnections getDatabaseConnections() {
        return databaseConnections;
    }

    /**
     * Sets the value of the databaseConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalConfiguration.DatabaseConnections }
     *     
     */
    public void setDatabaseConnections(LocalConfiguration.DatabaseConnections value) {
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
     *         &lt;element name="DatabaseConnection" type="{}DatabaseConnection" maxOccurs="2"/>
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
        "databaseConnection"
    })
    public static class DatabaseConnections {

        @XmlElement(name = "DatabaseConnection", required = true)
        protected List<DatabaseConnection> databaseConnection;

        /**
         * Gets the value of the databaseConnection property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the databaseConnection property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDatabaseConnection().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DatabaseConnection }
         * 
         * 
         */
        public List<DatabaseConnection> getDatabaseConnection() {
            if (databaseConnection == null) {
                databaseConnection = new ArrayList<DatabaseConnection>();
            }
            return this.databaseConnection;
        }

    }

}
