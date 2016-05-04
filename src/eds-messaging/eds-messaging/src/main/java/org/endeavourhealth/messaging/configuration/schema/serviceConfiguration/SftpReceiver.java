
package org.endeavourhealth.messaging.configuration.schema.serviceConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SftpReceiver complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SftpReceiver">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Path" type="{}nonEmptyString"/>
 *         &lt;element name="Filename" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="PollTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ReceiverClass" type="{}nonEmptyString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SftpReceiver", propOrder = {
    "path",
    "filename",
    "pollTime",
    "receiverClass"
})
public class SftpReceiver {

    @XmlElement(name = "Path", required = true)
    protected String path;
    @XmlElement(name = "Filename")
    protected String filename;
    @XmlElement(name = "PollTime")
    protected int pollTime;
    @XmlElement(name = "ReceiverClass", required = true)
    protected String receiverClass;

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
     * Gets the value of the pollTime property.
     * 
     */
    public int getPollTime() {
        return pollTime;
    }

    /**
     * Sets the value of the pollTime property.
     * 
     */
    public void setPollTime(int value) {
        this.pollTime = value;
    }

    /**
     * Gets the value of the receiverClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverClass() {
        return receiverClass;
    }

    /**
     * Sets the value of the receiverClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverClass(String value) {
        this.receiverClass = value;
    }

}
