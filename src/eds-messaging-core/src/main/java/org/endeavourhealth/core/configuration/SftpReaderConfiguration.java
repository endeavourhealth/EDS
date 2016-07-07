
package org.endeavourhealth.core.configuration;

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
 *         &lt;element name="Credentials">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Username" type="{}nonEmptyString"/>
 *                   &lt;element name="ClientPrivateKeyFilePath" type="{}nonEmptyString"/>
 *                   &lt;element name="ServerPublicKeyThumbprint" type="{}nonEmptyString"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Host" type="{}nonEmptyString"/>
 *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Path" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="Filename" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="Polltime" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "credentials",
    "host",
    "port",
    "path",
    "filename",
    "polltime",
    "pipeline"
})
@XmlRootElement(name = "SftpReaderConfiguration")
public class SftpReaderConfiguration {

    @XmlElement(name = "Credentials", required = true)
    protected SftpReaderConfiguration.Credentials credentials;
    @XmlElement(name = "Host", required = true)
    protected String host;
    @XmlElement(name = "Port")
    protected Integer port;
    @XmlElement(name = "Path")
    protected String path;
    @XmlElement(name = "Filename")
    protected String filename;
    @XmlElement(name = "Polltime")
    protected int polltime;
    @XmlElement(name = "Pipeline", required = true)
    protected Pipeline pipeline;

    /**
     * Gets the value of the credentials property.
     * 
     * @return
     *     possible object is
     *     {@link SftpReaderConfiguration.Credentials }
     *     
     */
    public SftpReaderConfiguration.Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the value of the credentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link SftpReaderConfiguration.Credentials }
     *     
     */
    public void setCredentials(SftpReaderConfiguration.Credentials value) {
        this.credentials = value;
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
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the filename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the value of the filename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Gets the value of the polltime property.
     * 
     */
    public int getPolltime() {
        return polltime;
    }

    /**
     * Sets the value of the polltime property.
     * 
     */
    public void setPolltime(int value) {
        this.polltime = value;
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
     *         &lt;element name="Username" type="{}nonEmptyString"/>
     *         &lt;element name="ClientPrivateKeyFilePath" type="{}nonEmptyString"/>
     *         &lt;element name="ServerPublicKeyThumbprint" type="{}nonEmptyString"/>
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
        "serverPublicKeyThumbprint"
    })
    public static class Credentials {

        @XmlElement(name = "Username", required = true)
        protected String username;
        @XmlElement(name = "ClientPrivateKeyFilePath", required = true)
        protected String clientPrivateKeyFilePath;
        @XmlElement(name = "ServerPublicKeyThumbprint", required = true)
        protected String serverPublicKeyThumbprint;

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
         * Gets the value of the serverPublicKeyThumbprint property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getServerPublicKeyThumbprint() {
            return serverPublicKeyThumbprint;
        }

        /**
         * Sets the value of the serverPublicKeyThumbprint property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setServerPublicKeyThumbprint(String value) {
            this.serverPublicKeyThumbprint = value;
        }

    }

}
