
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Nature of the EMIS PCS application that the user is using
 * e.g.GP, DDU (drug dependency) PO (podiatry)
 * 
 * <p>Java class for ApplicationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="ApplicationMnemonic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ApplicationName" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "applicationMnemonic",
    "applicationName"
})
public class ApplicationType
    extends IdentType
{

    @XmlElement(name = "ApplicationMnemonic", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String applicationMnemonic;
    @XmlElement(name = "ApplicationName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String applicationName;

    /**
     * Gets the value of the applicationMnemonic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationMnemonic() {
        return applicationMnemonic;
    }

    /**
     * Sets the value of the applicationMnemonic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationMnemonic(String value) {
        this.applicationMnemonic = value;
    }

    /**
     * Gets the value of the applicationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the value of the applicationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationName(String value) {
        this.applicationName = value;
    }

}
