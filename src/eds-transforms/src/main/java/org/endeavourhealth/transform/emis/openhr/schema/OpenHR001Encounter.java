
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Encounter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Encounter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="patient" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="effectiveTime" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
 *         &lt;element name="availabilityTimeStamp" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="authorisingUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="enteredByUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="confidentialityPolicy" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="consultationType" type="{http://www.e-mis.com/emisopen}voc.ConsultationType" minOccurs="0"/>
 *         &lt;element name="location" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="locationType" type="{http://www.e-mis.com/emisopen}OpenHR001.LocationType" minOccurs="0"/>
 *         &lt;element name="accompanyingHCP" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="duration" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
 *         &lt;element name="travelTime" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
 *         &lt;element name="appointmentEvent" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="template" type="{http://www.e-mis.com/emisopen}OpenHR001.TemplateIdentifier" minOccurs="0"/>
 *         &lt;element name="sectionData" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="component" type="{http://www.e-mis.com/emisopen}OpenHR001.Component" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="clinicalPurpose" type="{http://www.e-mis.com/emisopen}dt.CodeQualified" minOccurs="0"/>
 *         &lt;element name="externalConsulter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="complete" type="{http://www.e-mis.com/emisopen}dt.bool" default="true" />
 *       &lt;attribute name="removeComponentsOnDelete" type="{http://www.e-mis.com/emisopen}dt.bool" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Encounter", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "patient",
    "effectiveTime",
    "availabilityTimeStamp",
    "authorisingUserInRole",
    "enteredByUserInRole",
    "organisation",
    "confidentialityPolicy",
    "consultationType",
    "location",
    "locationType",
    "accompanyingHCP",
    "duration",
    "travelTime",
    "appointmentEvent",
    "template",
    "sectionData",
    "component",
    "clinicalPurpose",
    "externalConsulter"
})
public class OpenHR001Encounter
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String patient;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDatePart effectiveTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar availabilityTimeStamp;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String authorisingUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String enteredByUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String confidentialityPolicy;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocConsultationType consultationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String location;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001LocationType locationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> accompanyingHCP;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDuration duration;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDuration travelTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String appointmentEvent;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001TemplateIdentifier template;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String sectionData;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Component> component;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCodeQualified clinicalPurpose;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String externalConsulter;
    @XmlAttribute(name = "complete")
    protected Boolean complete;
    @XmlAttribute(name = "removeComponentsOnDelete")
    protected Boolean removeComponentsOnDelete;

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
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatient(String value) {
        this.patient = value;
    }

    /**
     * Gets the value of the effectiveTime property.
     * 
     * @return
     *     possible object is
     *     {@link DtDatePart }
     *     
     */
    public DtDatePart getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * Sets the value of the effectiveTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDatePart }
     *     
     */
    public void setEffectiveTime(DtDatePart value) {
        this.effectiveTime = value;
    }

    /**
     * Gets the value of the availabilityTimeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAvailabilityTimeStamp() {
        return availabilityTimeStamp;
    }

    /**
     * Sets the value of the availabilityTimeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAvailabilityTimeStamp(XMLGregorianCalendar value) {
        this.availabilityTimeStamp = value;
    }

    /**
     * Gets the value of the authorisingUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorisingUserInRole() {
        return authorisingUserInRole;
    }

    /**
     * Sets the value of the authorisingUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorisingUserInRole(String value) {
        this.authorisingUserInRole = value;
    }

    /**
     * Gets the value of the enteredByUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnteredByUserInRole() {
        return enteredByUserInRole;
    }

    /**
     * Sets the value of the enteredByUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnteredByUserInRole(String value) {
        this.enteredByUserInRole = value;
    }

    /**
     * Gets the value of the organisation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the organisation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganisation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOrganisation() {
        if (organisation == null) {
            organisation = new ArrayList<String>();
        }
        return this.organisation;
    }

    /**
     * Gets the value of the confidentialityPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfidentialityPolicy() {
        return confidentialityPolicy;
    }

    /**
     * Sets the value of the confidentialityPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfidentialityPolicy(String value) {
        this.confidentialityPolicy = value;
    }

    /**
     * Gets the value of the consultationType property.
     * 
     * @return
     *     possible object is
     *     {@link VocConsultationType }
     *     
     */
    public VocConsultationType getConsultationType() {
        return consultationType;
    }

    /**
     * Sets the value of the consultationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocConsultationType }
     *     
     */
    public void setConsultationType(VocConsultationType value) {
        this.consultationType = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the locationType property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public OpenHR001LocationType getLocationType() {
        return locationType;
    }

    /**
     * Sets the value of the locationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public void setLocationType(OpenHR001LocationType value) {
        this.locationType = value;
    }

    /**
     * Gets the value of the accompanyingHCP property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accompanyingHCP property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccompanyingHCP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAccompanyingHCP() {
        if (accompanyingHCP == null) {
            accompanyingHCP = new ArrayList<String>();
        }
        return this.accompanyingHCP;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link DtDuration }
     *     
     */
    public DtDuration getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDuration }
     *     
     */
    public void setDuration(DtDuration value) {
        this.duration = value;
    }

    /**
     * Gets the value of the travelTime property.
     * 
     * @return
     *     possible object is
     *     {@link DtDuration }
     *     
     */
    public DtDuration getTravelTime() {
        return travelTime;
    }

    /**
     * Sets the value of the travelTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDuration }
     *     
     */
    public void setTravelTime(DtDuration value) {
        this.travelTime = value;
    }

    /**
     * Gets the value of the appointmentEvent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppointmentEvent() {
        return appointmentEvent;
    }

    /**
     * Sets the value of the appointmentEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppointmentEvent(String value) {
        this.appointmentEvent = value;
    }

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001TemplateIdentifier }
     *     
     */
    public OpenHR001TemplateIdentifier getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001TemplateIdentifier }
     *     
     */
    public void setTemplate(OpenHR001TemplateIdentifier value) {
        this.template = value;
    }

    /**
     * Gets the value of the sectionData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSectionData() {
        return sectionData;
    }

    /**
     * Sets the value of the sectionData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSectionData(String value) {
        this.sectionData = value;
    }

    /**
     * Gets the value of the component property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the component property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Component }
     * 
     * 
     */
    public List<OpenHR001Component> getComponent() {
        if (component == null) {
            component = new ArrayList<OpenHR001Component>();
        }
        return this.component;
    }

    /**
     * Gets the value of the clinicalPurpose property.
     * 
     * @return
     *     possible object is
     *     {@link DtCodeQualified }
     *     
     */
    public DtCodeQualified getClinicalPurpose() {
        return clinicalPurpose;
    }

    /**
     * Sets the value of the clinicalPurpose property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCodeQualified }
     *     
     */
    public void setClinicalPurpose(DtCodeQualified value) {
        this.clinicalPurpose = value;
    }

    /**
     * Gets the value of the externalConsulter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalConsulter() {
        return externalConsulter;
    }

    /**
     * Sets the value of the externalConsulter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalConsulter(String value) {
        this.externalConsulter = value;
    }

    /**
     * Gets the value of the complete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isComplete() {
        if (complete == null) {
            return true;
        } else {
            return complete;
        }
    }

    /**
     * Sets the value of the complete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setComplete(Boolean value) {
        this.complete = value;
    }

    /**
     * Gets the value of the removeComponentsOnDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRemoveComponentsOnDelete() {
        if (removeComponentsOnDelete == null) {
            return false;
        } else {
            return removeComponentsOnDelete;
        }
    }

    /**
     * Sets the value of the removeComponentsOnDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveComponentsOnDelete(Boolean value) {
        this.removeComponentsOnDelete = value;
    }

}
