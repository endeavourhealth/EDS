
package org.endeavourhealth.transform.tpp.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientPlan complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientPlan">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PatientPlan" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Items" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="CarePlanInstruction" type="{}PatientPlanInstruction"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientPlan", propOrder = {
    "patientPlan",
    "items",
    "linkedProblemUID"
})
public class PatientPlan {

    @XmlElement(name = "PatientPlan", required = true)
    protected String patientPlan;
    @XmlElement(name = "Items")
    protected List<PatientPlan.Items> items;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the patientPlan property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientPlan() {
        return patientPlan;
    }

    /**
     * Sets the value of the patientPlan property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientPlan(String value) {
        this.patientPlan = value;
    }

    /**
     * Gets the value of the items property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the items property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientPlan.Items }
     * 
     * 
     */
    public List<PatientPlan.Items> getItems() {
        if (items == null) {
            items = new ArrayList<PatientPlan.Items>();
        }
        return this.items;
    }

    /**
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
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
     *         &lt;element name="CarePlanInstruction" type="{}PatientPlanInstruction"/>
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
        "carePlanInstruction"
    })
    public static class Items {

        @XmlElement(name = "CarePlanInstruction", required = true)
        protected PatientPlanInstruction carePlanInstruction;

        /**
         * Gets the value of the carePlanInstruction property.
         * 
         * @return
         *     possible object is
         *     {@link PatientPlanInstruction }
         *     
         */
        public PatientPlanInstruction getCarePlanInstruction() {
            return carePlanInstruction;
        }

        /**
         * Sets the value of the carePlanInstruction property.
         * 
         * @param value
         *     allowed object is
         *     {@link PatientPlanInstruction }
         *     
         */
        public void setCarePlanInstruction(PatientPlanInstruction value) {
            this.carePlanInstruction = value;
        }

    }

}
