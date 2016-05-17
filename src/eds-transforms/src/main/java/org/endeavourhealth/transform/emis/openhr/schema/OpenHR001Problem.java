
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.Problem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Problem">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="status" type="{http://www.e-mis.com/emisopen}voc.ProblemStatus" minOccurs="0"/>
 *         &lt;element name="significance" type="{http://www.e-mis.com/emisopen}voc.ProblemSignificance" minOccurs="0"/>
 *         &lt;element name="expectedDuration" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
 *         &lt;element name="endTime" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://www.e-mis.com/emisopen}voc.ProblemOwner" minOccurs="0"/>
 *         &lt;element name="parentProblem" type="{http://www.e-mis.com/emisopen}OpenHR001.ProblemHierarchy" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="childProblem" type="{http://www.e-mis.com/emisopen}OpenHR001.ProblemHierarchy" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="eventLink" type="{http://www.e-mis.com/emisopen}OpenHR001.ProblemEventLink" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Problem", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "status",
    "significance",
    "expectedDuration",
    "endTime",
    "owner",
    "parentProblem",
    "childProblem",
    "eventLink"
})
public class OpenHR001Problem
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocProblemStatus status;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocProblemSignificance significance;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDuration expectedDuration;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDatePart endTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocProblemOwner owner;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001ProblemHierarchy> parentProblem;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001ProblemHierarchy> childProblem;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001ProblemEventLink> eventLink;

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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link VocProblemStatus }
     *     
     */
    public VocProblemStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocProblemStatus }
     *     
     */
    public void setStatus(VocProblemStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the significance property.
     * 
     * @return
     *     possible object is
     *     {@link VocProblemSignificance }
     *     
     */
    public VocProblemSignificance getSignificance() {
        return significance;
    }

    /**
     * Sets the value of the significance property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocProblemSignificance }
     *     
     */
    public void setSignificance(VocProblemSignificance value) {
        this.significance = value;
    }

    /**
     * Gets the value of the expectedDuration property.
     * 
     * @return
     *     possible object is
     *     {@link DtDuration }
     *     
     */
    public DtDuration getExpectedDuration() {
        return expectedDuration;
    }

    /**
     * Sets the value of the expectedDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDuration }
     *     
     */
    public void setExpectedDuration(DtDuration value) {
        this.expectedDuration = value;
    }

    /**
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link DtDatePart }
     *     
     */
    public DtDatePart getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDatePart }
     *     
     */
    public void setEndTime(DtDatePart value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link VocProblemOwner }
     *     
     */
    public VocProblemOwner getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocProblemOwner }
     *     
     */
    public void setOwner(VocProblemOwner value) {
        this.owner = value;
    }

    /**
     * Gets the value of the parentProblem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parentProblem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParentProblem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001ProblemHierarchy }
     * 
     * 
     */
    public List<OpenHR001ProblemHierarchy> getParentProblem() {
        if (parentProblem == null) {
            parentProblem = new ArrayList<OpenHR001ProblemHierarchy>();
        }
        return this.parentProblem;
    }

    /**
     * Gets the value of the childProblem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the childProblem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChildProblem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001ProblemHierarchy }
     * 
     * 
     */
    public List<OpenHR001ProblemHierarchy> getChildProblem() {
        if (childProblem == null) {
            childProblem = new ArrayList<OpenHR001ProblemHierarchy>();
        }
        return this.childProblem;
    }

    /**
     * Gets the value of the eventLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001ProblemEventLink }
     * 
     * 
     */
    public List<OpenHR001ProblemEventLink> getEventLink() {
        if (eventLink == null) {
            eventLink = new ArrayList<OpenHR001ProblemEventLink>();
        }
        return this.eventLink;
    }

}
