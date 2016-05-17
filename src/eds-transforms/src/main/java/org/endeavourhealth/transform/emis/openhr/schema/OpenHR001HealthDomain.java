
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.HealthDomain complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.HealthDomain">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="encounter" type="{http://www.e-mis.com/emisopen}OpenHR001.Encounter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="event" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.Event">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="problem" type="{http://www.e-mis.com/emisopen}OpenHR001.Problem" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="document" type="{http://www.e-mis.com/emisopen}OpenHR001.Document" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="task" type="{http://www.e-mis.com/emisopen}OpenHR001.PatientTask" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.HealthDomain", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "encounter",
    "event",
    "problem",
    "document",
    "task"
})
public class OpenHR001HealthDomain {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Encounter> encounter;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001HealthDomain.Event> event;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Problem> problem;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Document> document;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001PatientTask> task;

    /**
     * Gets the value of the encounter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the encounter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEncounter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Encounter }
     * 
     * 
     */
    public List<OpenHR001Encounter> getEncounter() {
        if (encounter == null) {
            encounter = new ArrayList<OpenHR001Encounter>();
        }
        return this.encounter;
    }

    /**
     * Gets the value of the event property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the event property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001HealthDomain.Event }
     * 
     * 
     */
    public List<OpenHR001HealthDomain.Event> getEvent() {
        if (event == null) {
            event = new ArrayList<OpenHR001HealthDomain.Event>();
        }
        return this.event;
    }

    /**
     * Gets the value of the problem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the problem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProblem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Problem }
     * 
     * 
     */
    public List<OpenHR001Problem> getProblem() {
        if (problem == null) {
            problem = new ArrayList<OpenHR001Problem>();
        }
        return this.problem;
    }

    /**
     * Gets the value of the document property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the document property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Document }
     * 
     * 
     */
    public List<OpenHR001Document> getDocument() {
        if (document == null) {
            document = new ArrayList<OpenHR001Document>();
        }
        return this.document;
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
     * {@link OpenHR001PatientTask }
     * 
     * 
     */
    public List<OpenHR001PatientTask> getTask() {
        if (task == null) {
            task = new ArrayList<OpenHR001PatientTask>();
        }
        return this.task;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.Event">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Event
        extends OpenHR001Event
    {


    }

}
