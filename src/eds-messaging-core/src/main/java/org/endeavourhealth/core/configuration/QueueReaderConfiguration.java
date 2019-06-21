
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
 *         &lt;element name="Queue" type="{}nonEmptyString"/>
 *         &lt;element name="Exclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "queue",
    "exclusive",
    "pipeline"
})
@XmlRootElement(name = "QueueReaderConfiguration")
public class QueueReaderConfiguration {

    @XmlElement(name = "Queue", required = true)
    protected String queue;
    @XmlElement(name = "Exclusive")
    protected Boolean exclusive;
    @XmlElement(name = "Pipeline", required = true)
    protected Pipeline pipeline;

    /**
     * Gets the value of the queue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets the value of the queue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueue(String value) {
        this.queue = value;
    }

    /**
     * Gets the value of the exclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExclusive() {
        return exclusive;
    }

    /**
     * Sets the value of the exclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExclusive(Boolean value) {
        this.exclusive = value;
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

}
