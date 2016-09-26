
package org.endeavourhealth.subscriber.configuration.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;


/**
 * <p>Java class for PostgreSQLConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostgreSQLConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Hostname" type="{}nonEmptyString"/>
 *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Database" type="{}nonEmptyString"/>
 *         &lt;element name="Username" type="{}nonEmptyString"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostgreSQLConnection", propOrder = {
    "hostname",
    "port",
    "database",
    "username",
    "password"
})
public class PostgreSQLConnection {

    @XmlElement(name = "Hostname", required = true)
    protected String hostname;
    @XmlElement(name = "Port", required = true)
    protected BigInteger port;
    @XmlElement(name = "Database", required = true)
    protected String database;
    @XmlElement(name = "Username", required = true)
    protected String username;
    @XmlElement(name = "Password", required = true)
    protected String password;

    /**
     * Gets the value of the hostname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostname(String value) {
        this.hostname = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPort(BigInteger value) {
        this.port = value;
    }

    /**
     * Gets the value of the database property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the value of the database property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatabase(String value) {
        this.database = value;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

}
