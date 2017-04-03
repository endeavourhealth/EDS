
package org.endeavourhealth.transform.vitrucare.model;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for payload complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="payload">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="patientGUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dateofbirth" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="gender" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clinicalTerm" type="{}clinicalTerm" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="medication" type="{}medication" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "payload", propOrder = {
    "patientGUID",
    "dateofbirth",
    "gender",
    "clinicalTerm",
    "medication"
})
public class Payload {

    @XmlElement(required = true)
    protected String patientGUID;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateofbirth;
    protected String gender;
    protected List<ClinicalTerm> clinicalTerm;
    protected List<Medication> medication;

    /**
     * Gets the value of the patientGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientGUID() {
        return patientGUID;
    }

    /**
     * Sets the value of the patientGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientGUID(String value) {
        this.patientGUID = value;
    }

    /**
     * Gets the value of the dateofbirth property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateofbirth() {
        return dateofbirth;
    }

    /**
     * Sets the value of the dateofbirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateofbirth(XMLGregorianCalendar value) {
        this.dateofbirth = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGender(String value) {
        this.gender = value;
    }

    /**
     * Gets the value of the clinicalTerm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clinicalTerm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClinicalTerm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClinicalTerm }
     * 
     * 
     */
    public List<ClinicalTerm> getClinicalTerm() {
        if (clinicalTerm == null) {
            clinicalTerm = new ArrayList<ClinicalTerm>();
        }
        return this.clinicalTerm;
    }

    /**
     * Gets the value of the medication property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the medication property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMedication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Medication }
     * 
     * 
     */
    public List<Medication> getMedication() {
        if (medication == null) {
            medication = new ArrayList<Medication>();
        }
        return this.medication;
    }

}
