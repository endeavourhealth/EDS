
package org.endeavourhealth.core.configuration;

import java.math.BigInteger;
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
 *         &lt;element name="PostgresConnetion">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Hostname" type="{}nonEmptyString"/>
 *                   &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *                   &lt;element name="Database" type="{}nonEmptyString"/>
 *                   &lt;element name="Username" type="{}nonEmptyString"/>
 *                   &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "postgresConnetion"
})
@XmlRootElement(name = "SftpReaderConfiguration")
public class SftpReaderConfiguration {

    @XmlElement(name = "InstanceId", required = true)
    protected String instanceId;
    @XmlElement(name = "PostgresConnetion", required = true)
    protected SftpReaderConfiguration.PostgresConnetion postgresConnetion;

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
     * Gets the value of the postgresConnetion property.
     * 
     * @return
     *     possible object is
     *     {@link SftpReaderConfiguration.PostgresConnetion }
     *     
     */
    public SftpReaderConfiguration.PostgresConnetion getPostgresConnetion() {
        return postgresConnetion;
    }

    /**
     * Sets the value of the postgresConnetion property.
     * 
     * @param value
     *     allowed object is
     *     {@link SftpReaderConfiguration.PostgresConnetion }
     *     
     */
    public void setPostgresConnetion(SftpReaderConfiguration.PostgresConnetion value) {
        this.postgresConnetion = value;
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
    @XmlType(name = "", propOrder = {
        "hostname",
        "port",
        "database",
        "username",
        "password"
    })
    public static class PostgresConnetion {

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

}
