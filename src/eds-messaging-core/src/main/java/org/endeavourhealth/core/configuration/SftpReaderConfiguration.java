
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
 *         &lt;element name="SftpReaderIdentifier" type="{}nonEmptyString"/>
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
 *         &lt;element name="SftpCredentials">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Username" type="{}nonEmptyString"/>
 *                   &lt;element name="ClientPrivateKeyFilePath" type="{}nonEmptyString"/>
 *                   &lt;element name="ClientPrivateKeyPassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="HostPublicKeyFilePath" type="{}nonEmptyString"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Host" type="{}nonEmptyString"/>
 *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RemotePath" type="{}nonEmptyString"/>
 *         &lt;element name="LocalPath" type="{}nonEmptyString"/>
 *         &lt;element name="PgpDecryption">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PgpFileExtension" type="{}nonEmptyString"/>
 *                   &lt;element name="RecipientPrivateKeyFilePath" type="{}nonEmptyString"/>
 *                   &lt;element name="RecipientPrivateKeyPassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="SenderPublicKeyFilePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PollDelaySeconds" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Pipeline" type="{}Pipeline"/>
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
    "sftpReaderIdentifier",
    "postgresConnetion",
    "sftpCredentials",
    "host",
    "port",
    "remotePath",
    "localPath",
    "pgpDecryption",
    "pollDelaySeconds",
    "pipeline"
})
@XmlRootElement(name = "SftpReaderConfiguration")
public class SftpReaderConfiguration {

    @XmlElement(name = "SftpReaderIdentifier", required = true)
    protected String sftpReaderIdentifier;
    @XmlElement(name = "PostgresConnetion", required = true)
    protected SftpReaderConfiguration.PostgresConnetion postgresConnetion;
    @XmlElement(name = "SftpCredentials", required = true)
    protected SftpReaderConfiguration.SftpCredentials sftpCredentials;
    @XmlElement(name = "Host", required = true)
    protected String host;
    @XmlElement(name = "Port")
    protected Integer port;
    @XmlElement(name = "RemotePath", required = true)
    protected String remotePath;
    @XmlElement(name = "LocalPath", required = true)
    protected String localPath;
    @XmlElement(name = "PgpDecryption", required = true)
    protected SftpReaderConfiguration.PgpDecryption pgpDecryption;
    @XmlElement(name = "PollDelaySeconds")
    protected int pollDelaySeconds;
    @XmlElement(name = "Pipeline", required = true)
    protected Pipeline pipeline;

    /**
     * Gets the value of the sftpReaderIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSftpReaderIdentifier() {
        return sftpReaderIdentifier;
    }

    /**
     * Sets the value of the sftpReaderIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSftpReaderIdentifier(String value) {
        this.sftpReaderIdentifier = value;
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
     * Gets the value of the sftpCredentials property.
     * 
     * @return
     *     possible object is
     *     {@link SftpReaderConfiguration.SftpCredentials }
     *     
     */
    public SftpReaderConfiguration.SftpCredentials getSftpCredentials() {
        return sftpCredentials;
    }

    /**
     * Sets the value of the sftpCredentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link SftpReaderConfiguration.SftpCredentials }
     *     
     */
    public void setSftpCredentials(SftpReaderConfiguration.SftpCredentials value) {
        this.sftpCredentials = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the remotePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemotePath() {
        return remotePath;
    }

    /**
     * Sets the value of the remotePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemotePath(String value) {
        this.remotePath = value;
    }

    /**
     * Gets the value of the localPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalPath() {
        return localPath;
    }

    /**
     * Sets the value of the localPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalPath(String value) {
        this.localPath = value;
    }

    /**
     * Gets the value of the pgpDecryption property.
     * 
     * @return
     *     possible object is
     *     {@link SftpReaderConfiguration.PgpDecryption }
     *     
     */
    public SftpReaderConfiguration.PgpDecryption getPgpDecryption() {
        return pgpDecryption;
    }

    /**
     * Sets the value of the pgpDecryption property.
     * 
     * @param value
     *     allowed object is
     *     {@link SftpReaderConfiguration.PgpDecryption }
     *     
     */
    public void setPgpDecryption(SftpReaderConfiguration.PgpDecryption value) {
        this.pgpDecryption = value;
    }

    /**
     * Gets the value of the pollDelaySeconds property.
     * 
     */
    public int getPollDelaySeconds() {
        return pollDelaySeconds;
    }

    /**
     * Sets the value of the pollDelaySeconds property.
     * 
     */
    public void setPollDelaySeconds(int value) {
        this.pollDelaySeconds = value;
    }

    /**
     * Gets the value of the pipeline property.
     * 
     * @return
     *     possible object is
     *     {@link Pipeline }
     *     
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Sets the value of the pipeline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pipeline }
     *     
     */
    public void setPipeline(Pipeline value) {
        this.pipeline = value;
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
     *         &lt;element name="PgpFileExtension" type="{}nonEmptyString"/>
     *         &lt;element name="RecipientPrivateKeyFilePath" type="{}nonEmptyString"/>
     *         &lt;element name="RecipientPrivateKeyPassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="SenderPublicKeyFilePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "pgpFileExtension",
        "recipientPrivateKeyFilePath",
        "recipientPrivateKeyPassword",
        "senderPublicKeyFilePath"
    })
    public static class PgpDecryption {

        @XmlElement(name = "PgpFileExtension", required = true)
        protected String pgpFileExtension;
        @XmlElement(name = "RecipientPrivateKeyFilePath", required = true)
        protected String recipientPrivateKeyFilePath;
        @XmlElement(name = "RecipientPrivateKeyPassword", required = true)
        protected String recipientPrivateKeyPassword;
        @XmlElement(name = "SenderPublicKeyFilePath", required = true)
        protected String senderPublicKeyFilePath;

        /**
         * Gets the value of the pgpFileExtension property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPgpFileExtension() {
            return pgpFileExtension;
        }

        /**
         * Sets the value of the pgpFileExtension property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPgpFileExtension(String value) {
            this.pgpFileExtension = value;
        }

        /**
         * Gets the value of the recipientPrivateKeyFilePath property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRecipientPrivateKeyFilePath() {
            return recipientPrivateKeyFilePath;
        }

        /**
         * Sets the value of the recipientPrivateKeyFilePath property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRecipientPrivateKeyFilePath(String value) {
            this.recipientPrivateKeyFilePath = value;
        }

        /**
         * Gets the value of the recipientPrivateKeyPassword property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRecipientPrivateKeyPassword() {
            return recipientPrivateKeyPassword;
        }

        /**
         * Sets the value of the recipientPrivateKeyPassword property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRecipientPrivateKeyPassword(String value) {
            this.recipientPrivateKeyPassword = value;
        }

        /**
         * Gets the value of the senderPublicKeyFilePath property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSenderPublicKeyFilePath() {
            return senderPublicKeyFilePath;
        }

        /**
         * Sets the value of the senderPublicKeyFilePath property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSenderPublicKeyFilePath(String value) {
            this.senderPublicKeyFilePath = value;
        }

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
     *         &lt;element name="Username" type="{}nonEmptyString"/>
     *         &lt;element name="ClientPrivateKeyFilePath" type="{}nonEmptyString"/>
     *         &lt;element name="ClientPrivateKeyPassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="HostPublicKeyFilePath" type="{}nonEmptyString"/>
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
        "username",
        "clientPrivateKeyFilePath",
        "clientPrivateKeyPassword",
        "hostPublicKeyFilePath"
    })
    public static class SftpCredentials {

        @XmlElement(name = "Username", required = true)
        protected String username;
        @XmlElement(name = "ClientPrivateKeyFilePath", required = true)
        protected String clientPrivateKeyFilePath;
        @XmlElement(name = "ClientPrivateKeyPassword", required = true)
        protected String clientPrivateKeyPassword;
        @XmlElement(name = "HostPublicKeyFilePath", required = true)
        protected String hostPublicKeyFilePath;

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
         * Gets the value of the clientPrivateKeyFilePath property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getClientPrivateKeyFilePath() {
            return clientPrivateKeyFilePath;
        }

        /**
         * Sets the value of the clientPrivateKeyFilePath property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setClientPrivateKeyFilePath(String value) {
            this.clientPrivateKeyFilePath = value;
        }

        /**
         * Gets the value of the clientPrivateKeyPassword property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getClientPrivateKeyPassword() {
            return clientPrivateKeyPassword;
        }

        /**
         * Sets the value of the clientPrivateKeyPassword property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setClientPrivateKeyPassword(String value) {
            this.clientPrivateKeyPassword = value;
        }

        /**
         * Gets the value of the hostPublicKeyFilePath property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHostPublicKeyFilePath() {
            return hostPublicKeyFilePath;
        }

        /**
         * Sets the value of the hostPublicKeyFilePath property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHostPublicKeyFilePath(String value) {
            this.hostPublicKeyFilePath = value;
        }

    }

}
