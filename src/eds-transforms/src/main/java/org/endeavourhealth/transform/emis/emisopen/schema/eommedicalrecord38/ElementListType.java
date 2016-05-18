
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ElementListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ElementListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ConsultationElement" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *                 &lt;sequence>
 *                   &lt;element name="DisplayOrder" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
 *                   &lt;element name="ProblemSection" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *                   &lt;element name="Header" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *                   &lt;choice>
 *                     &lt;element name="Event" type="{http://www.e-mis.com/emisopen/MedicalRecord}EventType" minOccurs="0"/>
 *                     &lt;element name="Medication" type="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationType" minOccurs="0"/>
 *                     &lt;element name="Diary" type="{http://www.e-mis.com/emisopen/MedicalRecord}DiaryType" minOccurs="0"/>
 *                     &lt;element name="Referral" type="{http://www.e-mis.com/emisopen/MedicalRecord}ReferralType" minOccurs="0"/>
 *                     &lt;element name="Allergy" type="{http://www.e-mis.com/emisopen/MedicalRecord}AllergyType" minOccurs="0"/>
 *                     &lt;element name="Attachment" type="{http://www.e-mis.com/emisopen/MedicalRecord}AttachmentType" minOccurs="0"/>
 *                     &lt;element name="TestRequest" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestHeaderType" minOccurs="0"/>
 *                     &lt;element name="Investigation" type="{http://www.e-mis.com/emisopen/MedicalRecord}InvestigationType" minOccurs="0"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ElementListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "consultationElement"
})
public class ElementListType {

    @XmlElement(name = "ConsultationElement", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<ElementListType.ConsultationElement> consultationElement;

    /**
     * Gets the value of the consultationElement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consultationElement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsultationElement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ElementListType.ConsultationElement }
     * 
     * 
     */
    public List<ElementListType.ConsultationElement> getConsultationElement() {
        if (consultationElement == null) {
            consultationElement = new ArrayList<ElementListType.ConsultationElement>();
        }
        return this.consultationElement;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
     *       &lt;sequence>
     *         &lt;element name="DisplayOrder" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
     *         &lt;element name="ProblemSection" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
     *         &lt;element name="Header" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
     *         &lt;choice>
     *           &lt;element name="Event" type="{http://www.e-mis.com/emisopen/MedicalRecord}EventType" minOccurs="0"/>
     *           &lt;element name="Medication" type="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationType" minOccurs="0"/>
     *           &lt;element name="Diary" type="{http://www.e-mis.com/emisopen/MedicalRecord}DiaryType" minOccurs="0"/>
     *           &lt;element name="Referral" type="{http://www.e-mis.com/emisopen/MedicalRecord}ReferralType" minOccurs="0"/>
     *           &lt;element name="Allergy" type="{http://www.e-mis.com/emisopen/MedicalRecord}AllergyType" minOccurs="0"/>
     *           &lt;element name="Attachment" type="{http://www.e-mis.com/emisopen/MedicalRecord}AttachmentType" minOccurs="0"/>
     *           &lt;element name="TestRequest" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestHeaderType" minOccurs="0"/>
     *           &lt;element name="Investigation" type="{http://www.e-mis.com/emisopen/MedicalRecord}InvestigationType" minOccurs="0"/>
     *         &lt;/choice>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "displayOrder",
        "problemSection",
        "header",
        "event",
        "medication",
        "diary",
        "referral",
        "allergy",
        "attachment",
        "testRequest",
        "investigation"
    })
    public static class ConsultationElement
        extends IdentType
    {

        @XmlElement(name = "DisplayOrder", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected Short displayOrder;
        @XmlElement(name = "ProblemSection", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected Byte problemSection;
        @XmlElement(name = "Header", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected IntegerCodeType header;
        @XmlElement(name = "Event", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected EventType event;
        @XmlElement(name = "Medication", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected MedicationType medication;
        @XmlElement(name = "Diary", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected DiaryType diary;
        @XmlElement(name = "Referral", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected ReferralType referral;
        @XmlElement(name = "Allergy", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected AllergyType allergy;
        @XmlElement(name = "Attachment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected AttachmentType attachment;
        @XmlElement(name = "TestRequest", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected TestRequestHeaderType testRequest;
        @XmlElement(name = "Investigation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected InvestigationType investigation;

        /**
         * Gets the value of the displayOrder property.
         * 
         * @return
         *     possible object is
         *     {@link Short }
         *     
         */
        public Short getDisplayOrder() {
            return displayOrder;
        }

        /**
         * Sets the value of the displayOrder property.
         * 
         * @param value
         *     allowed object is
         *     {@link Short }
         *     
         */
        public void setDisplayOrder(Short value) {
            this.displayOrder = value;
        }

        /**
         * Gets the value of the problemSection property.
         * 
         * @return
         *     possible object is
         *     {@link Byte }
         *     
         */
        public Byte getProblemSection() {
            return problemSection;
        }

        /**
         * Sets the value of the problemSection property.
         * 
         * @param value
         *     allowed object is
         *     {@link Byte }
         *     
         */
        public void setProblemSection(Byte value) {
            this.problemSection = value;
        }

        /**
         * Gets the value of the header property.
         * 
         * @return
         *     possible object is
         *     {@link IntegerCodeType }
         *     
         */
        public IntegerCodeType getHeader() {
            return header;
        }

        /**
         * Sets the value of the header property.
         * 
         * @param value
         *     allowed object is
         *     {@link IntegerCodeType }
         *     
         */
        public void setHeader(IntegerCodeType value) {
            this.header = value;
        }

        /**
         * Gets the value of the event property.
         * 
         * @return
         *     possible object is
         *     {@link EventType }
         *     
         */
        public EventType getEvent() {
            return event;
        }

        /**
         * Sets the value of the event property.
         * 
         * @param value
         *     allowed object is
         *     {@link EventType }
         *     
         */
        public void setEvent(EventType value) {
            this.event = value;
        }

        /**
         * Gets the value of the medication property.
         * 
         * @return
         *     possible object is
         *     {@link MedicationType }
         *     
         */
        public MedicationType getMedication() {
            return medication;
        }

        /**
         * Sets the value of the medication property.
         * 
         * @param value
         *     allowed object is
         *     {@link MedicationType }
         *     
         */
        public void setMedication(MedicationType value) {
            this.medication = value;
        }

        /**
         * Gets the value of the diary property.
         * 
         * @return
         *     possible object is
         *     {@link DiaryType }
         *     
         */
        public DiaryType getDiary() {
            return diary;
        }

        /**
         * Sets the value of the diary property.
         * 
         * @param value
         *     allowed object is
         *     {@link DiaryType }
         *     
         */
        public void setDiary(DiaryType value) {
            this.diary = value;
        }

        /**
         * Gets the value of the referral property.
         * 
         * @return
         *     possible object is
         *     {@link ReferralType }
         *     
         */
        public ReferralType getReferral() {
            return referral;
        }

        /**
         * Sets the value of the referral property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReferralType }
         *     
         */
        public void setReferral(ReferralType value) {
            this.referral = value;
        }

        /**
         * Gets the value of the allergy property.
         * 
         * @return
         *     possible object is
         *     {@link AllergyType }
         *     
         */
        public AllergyType getAllergy() {
            return allergy;
        }

        /**
         * Sets the value of the allergy property.
         * 
         * @param value
         *     allowed object is
         *     {@link AllergyType }
         *     
         */
        public void setAllergy(AllergyType value) {
            this.allergy = value;
        }

        /**
         * Gets the value of the attachment property.
         * 
         * @return
         *     possible object is
         *     {@link AttachmentType }
         *     
         */
        public AttachmentType getAttachment() {
            return attachment;
        }

        /**
         * Sets the value of the attachment property.
         * 
         * @param value
         *     allowed object is
         *     {@link AttachmentType }
         *     
         */
        public void setAttachment(AttachmentType value) {
            this.attachment = value;
        }

        /**
         * Gets the value of the testRequest property.
         * 
         * @return
         *     possible object is
         *     {@link TestRequestHeaderType }
         *     
         */
        public TestRequestHeaderType getTestRequest() {
            return testRequest;
        }

        /**
         * Sets the value of the testRequest property.
         * 
         * @param value
         *     allowed object is
         *     {@link TestRequestHeaderType }
         *     
         */
        public void setTestRequest(TestRequestHeaderType value) {
            this.testRequest = value;
        }

        /**
         * Gets the value of the investigation property.
         * 
         * @return
         *     possible object is
         *     {@link InvestigationType }
         *     
         */
        public InvestigationType getInvestigation() {
            return investigation;
        }

        /**
         * Sets the value of the investigation property.
         * 
         * @param value
         *     allowed object is
         *     {@link InvestigationType }
         *     
         */
        public void setInvestigation(InvestigationType value) {
            this.investigation = value;
        }

    }

}
