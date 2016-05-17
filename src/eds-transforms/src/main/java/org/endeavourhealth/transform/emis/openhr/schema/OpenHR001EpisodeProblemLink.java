
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.EpisodeProblemLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.EpisodeProblemLink">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="problem" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.EpisodeProblemLink", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "problem"
})
public class OpenHR001EpisodeProblemLink
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String problem;

    /**
     * Gets the value of the problem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProblem() {
        return problem;
    }

    /**
     * Sets the value of the problem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProblem(String value) {
        this.problem = value;
    }

}
