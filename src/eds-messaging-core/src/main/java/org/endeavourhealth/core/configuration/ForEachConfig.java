
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ForEachConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ForEachConfig">
 *   &lt;complexContent>
 *     &lt;extension base="{}ComponentConfig">
 *       &lt;sequence>
 *         &lt;element name="Header" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="InList" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="InCsv" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element name="Pipeline" type="{}Pipeline"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ForEachConfig", propOrder = {
    "header",
    "inList",
    "inCsv",
    "pipeline"
})
public class ForEachConfig
    extends ComponentConfig
{

    @XmlElement(name = "Header", required = true)
    protected String header;
    @XmlElement(name = "InList")
    protected String inList;
    @XmlElement(name = "InCsv")
    protected String inCsv;
    @XmlElement(name = "Pipeline", required = true)
    protected Pipeline pipeline;

    /**
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeader(String value) {
        this.header = value;
    }

    /**
     * Gets the value of the inList property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInList() {
        return inList;
    }

    /**
     * Sets the value of the inList property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInList(String value) {
        this.inList = value;
    }

    /**
     * Gets the value of the inCsv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInCsv() {
        return inCsv;
    }

    /**
     * Sets the value of the inCsv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInCsv(String value) {
        this.inCsv = value;
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
