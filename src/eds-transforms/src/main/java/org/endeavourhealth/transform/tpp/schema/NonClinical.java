
package org.endeavourhealth.transform.tpp.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonClinical complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonClinical">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Appointment" type="{}Appointment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Visit" type="{}Visit" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Task" type="{}Task" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonClinical", propOrder = {
    "appointment",
    "visit",
    "task"
})
public class NonClinical {

    @XmlElement(name = "Appointment")
    protected List<Appointment> appointment;
    @XmlElement(name = "Visit")
    protected List<Visit> visit;
    @XmlElement(name = "Task")
    protected List<Task> task;

    /**
     * Gets the value of the appointment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appointment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppointment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Appointment }
     * 
     * 
     */
    public List<Appointment> getAppointment() {
        if (appointment == null) {
            appointment = new ArrayList<Appointment>();
        }
        return this.appointment;
    }

    /**
     * Gets the value of the visit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the visit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVisit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Visit }
     * 
     * 
     */
    public List<Visit> getVisit() {
        if (visit == null) {
            visit = new ArrayList<Visit>();
        }
        return this.visit;
    }

    /**
     * Gets the value of the task property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the task property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTask().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Task }
     * 
     * 
     */
    public List<Task> getTask() {
        if (task == null) {
            task = new ArrayList<Task>();
        }
        return this.task;
    }

}
