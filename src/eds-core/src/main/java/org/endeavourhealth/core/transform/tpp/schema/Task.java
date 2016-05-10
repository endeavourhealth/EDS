
package org.endeavourhealth.core.transform.tpp.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Task complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Task">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TaskType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Due" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Content" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UserNameAssigned" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GroupNameAssigned" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Task", propOrder = {
    "taskType",
    "due",
    "content",
    "userNameAssigned",
    "groupNameAssigned"
})
public class Task {

    @XmlElement(name = "TaskType", required = true)
    protected String taskType;
    @XmlElement(name = "Due")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar due;
    @XmlElement(name = "Content", required = true)
    protected String content;
    @XmlElement(name = "UserNameAssigned")
    protected String userNameAssigned;
    @XmlElement(name = "GroupNameAssigned")
    protected String groupNameAssigned;

    /**
     * Gets the value of the taskType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * Sets the value of the taskType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskType(String value) {
        this.taskType = value;
    }

    /**
     * Gets the value of the due property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDue() {
        return due;
    }

    /**
     * Sets the value of the due property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDue(XMLGregorianCalendar value) {
        this.due = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Gets the value of the userNameAssigned property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserNameAssigned() {
        return userNameAssigned;
    }

    /**
     * Sets the value of the userNameAssigned property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserNameAssigned(String value) {
        this.userNameAssigned = value;
    }

    /**
     * Gets the value of the groupNameAssigned property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupNameAssigned() {
        return groupNameAssigned;
    }

    /**
     * Sets the value of the groupNameAssigned property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupNameAssigned(String value) {
        this.groupNameAssigned = value;
    }

}
