
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.CaseloadPatient complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.CaseloadPatient">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="dateRecorded" type="{http://www.e-mis.com/emisopen}dt.DateTime"/>
 *         &lt;element name="dateEnded" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="patientStatus" type="{http://www.e-mis.com/emisopen}OpenHR001.PatientStatus"/>
 *         &lt;element name="patientType">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.e-mis.com/emisopen>voc.PatientType">
 *                 &lt;attribute name="dummy" type="{http://www.e-mis.com/emisopen}dt.bool" fixed="false" />
 *                 &lt;attribute name="userDefined" type="{http://www.e-mis.com/emisopen}dt.bool" fixed="false" />
 *                 &lt;attribute name="originalTerm" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="property" type="{http://www.e-mis.com/emisopen}OpenHR001.CaseloadPatientProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="customRegistrationField" type="{http://www.e-mis.com/emisopen}OpenHR001.CustomRegistration" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="emisPatientNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="preferredHCPUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.CaseloadPatient", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "organisation",
    "dateRecorded",
    "dateEnded",
    "patientStatus",
    "patientType",
    "property",
    "customRegistrationField",
    "emisPatientNumber",
    "preferredHCPUserInRole"
})
public class OpenHR001CaseloadPatient
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateRecorded;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateEnded;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected OpenHR001PatientStatus patientStatus;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected OpenHR001CaseloadPatient.PatientType patientType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001CaseloadPatientProperty> property;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001CustomRegistration> customRegistrationField;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer emisPatientNumber;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String preferredHCPUserInRole;

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
     * Gets the value of the organisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisation() {
        return organisation;
    }

    /**
     * Sets the value of the organisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisation(String value) {
        this.organisation = value;
    }

    /**
     * Gets the value of the dateRecorded property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateRecorded() {
        return dateRecorded;
    }

    /**
     * Sets the value of the dateRecorded property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateRecorded(XMLGregorianCalendar value) {
        this.dateRecorded = value;
    }

    /**
     * Gets the value of the dateEnded property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateEnded() {
        return dateEnded;
    }

    /**
     * Sets the value of the dateEnded property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateEnded(XMLGregorianCalendar value) {
        this.dateEnded = value;
    }

    /**
     * Gets the value of the patientStatus property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001PatientStatus }
     *     
     */
    public OpenHR001PatientStatus getPatientStatus() {
        return patientStatus;
    }

    /**
     * Sets the value of the patientStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001PatientStatus }
     *     
     */
    public void setPatientStatus(OpenHR001PatientStatus value) {
        this.patientStatus = value;
    }

    /**
     * Gets the value of the patientType property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001CaseloadPatient.PatientType }
     *     
     */
    public OpenHR001CaseloadPatient.PatientType getPatientType() {
        return patientType;
    }

    /**
     * Sets the value of the patientType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001CaseloadPatient.PatientType }
     *     
     */
    public void setPatientType(OpenHR001CaseloadPatient.PatientType value) {
        this.patientType = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001CaseloadPatientProperty }
     * 
     * 
     */
    public List<OpenHR001CaseloadPatientProperty> getProperty() {
        if (property == null) {
            property = new ArrayList<OpenHR001CaseloadPatientProperty>();
        }
        return this.property;
    }

    /**
     * Gets the value of the customRegistrationField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customRegistrationField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomRegistrationField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001CustomRegistration }
     * 
     * 
     */
    public List<OpenHR001CustomRegistration> getCustomRegistrationField() {
        if (customRegistrationField == null) {
            customRegistrationField = new ArrayList<OpenHR001CustomRegistration>();
        }
        return this.customRegistrationField;
    }

    /**
     * Gets the value of the emisPatientNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEmisPatientNumber() {
        return emisPatientNumber;
    }

    /**
     * Sets the value of the emisPatientNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEmisPatientNumber(Integer value) {
        this.emisPatientNumber = value;
    }

    /**
     * Gets the value of the preferredHCPUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferredHCPUserInRole() {
        return preferredHCPUserInRole;
    }

    /**
     * Sets the value of the preferredHCPUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferredHCPUserInRole(String value) {
        this.preferredHCPUserInRole = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.e-mis.com/emisopen>voc.PatientType">
     *       &lt;attribute name="dummy" type="{http://www.e-mis.com/emisopen}dt.bool" fixed="false" />
     *       &lt;attribute name="userDefined" type="{http://www.e-mis.com/emisopen}dt.bool" fixed="false" />
     *       &lt;attribute name="originalTerm" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class PatientType {

        @XmlValue
        protected VocPatientType value;
        @XmlAttribute(name = "dummy")
        protected Boolean dummy;
        @XmlAttribute(name = "userDefined")
        protected Boolean userDefined;
        @XmlAttribute(name = "originalTerm")
        protected String originalTerm;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link VocPatientType }
         *     
         */
        public VocPatientType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link VocPatientType }
         *     
         */
        public void setValue(VocPatientType value) {
            this.value = value;
        }

        /**
         * Gets the value of the dummy property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isDummy() {
            if (dummy == null) {
                return false;
            } else {
                return dummy;
            }
        }

        /**
         * Sets the value of the dummy property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setDummy(Boolean value) {
            this.dummy = value;
        }

        /**
         * Gets the value of the userDefined property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isUserDefined() {
            if (userDefined == null) {
                return false;
            } else {
                return userDefined;
            }
        }

        /**
         * Sets the value of the userDefined property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setUserDefined(Boolean value) {
            this.userDefined = value;
        }

        /**
         * Gets the value of the originalTerm property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOriginalTerm() {
            return originalTerm;
        }

        /**
         * Sets the value of the originalTerm property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOriginalTerm(String value) {
            this.originalTerm = value;
        }

    }

}
