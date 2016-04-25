
package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReceivePortType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReceivePortType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="Http">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                     &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="Methods" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="IncludeSubPaths" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="Sftp">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="RabbitMQ">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="QueueName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element name="ReceivePortClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReceivePortType", propOrder = {
    "id",
    "http",
    "sftp",
    "rabbitMQ",
    "receivePortClass"
})
public class ReceivePortType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "Http")
    protected ReceivePortType.Http http;
    @XmlElement(name = "Sftp")
    protected ReceivePortType.Sftp sftp;
    @XmlElement(name = "RabbitMQ")
    protected ReceivePortType.RabbitMQ rabbitMQ;
    @XmlElement(name = "ReceivePortClass", required = true)
    protected String receivePortClass;

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
     * Gets the value of the http property.
     * 
     * @return
     *     possible object is
     *     {@link ReceivePortType.Http }
     *     
     */
    public ReceivePortType.Http getHttp() {
        return http;
    }

    /**
     * Sets the value of the http property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReceivePortType.Http }
     *     
     */
    public void setHttp(ReceivePortType.Http value) {
        this.http = value;
    }

    /**
     * Gets the value of the sftp property.
     * 
     * @return
     *     possible object is
     *     {@link ReceivePortType.Sftp }
     *     
     */
    public ReceivePortType.Sftp getSftp() {
        return sftp;
    }

    /**
     * Sets the value of the sftp property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReceivePortType.Sftp }
     *     
     */
    public void setSftp(ReceivePortType.Sftp value) {
        this.sftp = value;
    }

    /**
     * Gets the value of the rabbitMQ property.
     * 
     * @return
     *     possible object is
     *     {@link ReceivePortType.RabbitMQ }
     *     
     */
    public ReceivePortType.RabbitMQ getRabbitMQ() {
        return rabbitMQ;
    }

    /**
     * Sets the value of the rabbitMQ property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReceivePortType.RabbitMQ }
     *     
     */
    public void setRabbitMQ(ReceivePortType.RabbitMQ value) {
        this.rabbitMQ = value;
    }

    /**
     * Gets the value of the receivePortClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivePortClass() {
        return receivePortClass;
    }

    /**
     * Sets the value of the receivePortClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivePortClass(String value) {
        this.receivePortClass = value;
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
     *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="Methods" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="IncludeSubPaths" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "port",
        "path",
        "methods",
        "includeSubPaths"
    })
    public static class Http {

        @XmlElement(name = "Port")
        protected int port;
        @XmlElement(name = "Path", required = true)
        protected String path;
        @XmlElement(name = "Methods", required = true)
        protected String methods;
        @XmlElement(name = "IncludeSubPaths", required = true)
        protected String includeSubPaths;

        /**
         * Gets the value of the port property.
         * 
         */
        public int getPort() {
            return port;
        }

        /**
         * Sets the value of the port property.
         * 
         */
        public void setPort(int value) {
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
         * Gets the value of the methods property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMethods() {
            return methods;
        }

        /**
         * Sets the value of the methods property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMethods(String value) {
            this.methods = value;
        }

        /**
         * Gets the value of the includeSubPaths property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIncludeSubPaths() {
            return includeSubPaths;
        }

        /**
         * Sets the value of the includeSubPaths property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIncludeSubPaths(String value) {
            this.includeSubPaths = value;
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
     *         &lt;element name="QueueName" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "queueName"
    })
    public static class RabbitMQ {

        @XmlElement(name = "QueueName", required = true)
        protected String queueName;

        /**
         * Gets the value of the queueName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getQueueName() {
            return queueName;
        }

        /**
         * Sets the value of the queueName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setQueueName(String value) {
            this.queueName = value;
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
     *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "path"
    })
    public static class Sftp {

        @XmlElement(name = "Path", required = true)
        protected String path;

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

    }

}
