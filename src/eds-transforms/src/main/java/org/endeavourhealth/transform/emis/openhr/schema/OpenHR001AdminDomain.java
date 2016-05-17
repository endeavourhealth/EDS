
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.AdminDomain complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.AdminDomain">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="person" type="{http://www.e-mis.com/emisopen}OpenHR001.Person" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="patient" type="{http://www.e-mis.com/emisopen}OpenHR001.Patient" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}OpenHR001.Organisation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="location" type="{http://www.e-mis.com/emisopen}OpenHR001.Location" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="user" type="{http://www.e-mis.com/emisopen}OpenHR001.User" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="role" type="{http://www.e-mis.com/emisopen}OpenHR001.Role" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="userInRole" type="{http://www.e-mis.com/emisopen}OpenHR001.UserInRole" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="localMixture" type="{http://www.e-mis.com/emisopen}OpenHR001.LocalMixture" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="auditTrail" type="{http://www.e-mis.com/emisopen}OpenHR001.AuditTrail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="confidentialityPolicy" type="{http://www.e-mis.com/emisopen}OpenHR001.ConfidentialityPolicy" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.AdminDomain", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "person",
    "patient",
    "organisation",
    "location",
    "user",
    "role",
    "userInRole",
    "localMixture",
    "auditTrail",
    "confidentialityPolicy"
})
public class OpenHR001AdminDomain {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Person> person;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Patient> patient;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Organisation> organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Location> location;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001User> user;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Role> role;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001UserInRole> userInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001LocalMixture> localMixture;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001AuditTrail> auditTrail;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001ConfidentialityPolicy> confidentialityPolicy;

    /**
     * Gets the value of the person property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the person property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPerson().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Person }
     * 
     * 
     */
    public List<OpenHR001Person> getPerson() {
        if (person == null) {
            person = new ArrayList<OpenHR001Person>();
        }
        return this.person;
    }

    /**
     * Gets the value of the patient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Patient }
     * 
     * 
     */
    public List<OpenHR001Patient> getPatient() {
        if (patient == null) {
            patient = new ArrayList<OpenHR001Patient>();
        }
        return this.patient;
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
     * {@link OpenHR001Organisation }
     * 
     * 
     */
    public List<OpenHR001Organisation> getOrganisation() {
        if (organisation == null) {
            organisation = new ArrayList<OpenHR001Organisation>();
        }
        return this.organisation;
    }

    /**
     * Gets the value of the location property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the location property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Location }
     * 
     * 
     */
    public List<OpenHR001Location> getLocation() {
        if (location == null) {
            location = new ArrayList<OpenHR001Location>();
        }
        return this.location;
    }

    /**
     * Gets the value of the user property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the user property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001User }
     * 
     * 
     */
    public List<OpenHR001User> getUser() {
        if (user == null) {
            user = new ArrayList<OpenHR001User>();
        }
        return this.user;
    }

    /**
     * Gets the value of the role property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the role property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Role }
     * 
     * 
     */
    public List<OpenHR001Role> getRole() {
        if (role == null) {
            role = new ArrayList<OpenHR001Role>();
        }
        return this.role;
    }

    /**
     * Gets the value of the userInRole property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userInRole property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserInRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001UserInRole }
     * 
     * 
     */
    public List<OpenHR001UserInRole> getUserInRole() {
        if (userInRole == null) {
            userInRole = new ArrayList<OpenHR001UserInRole>();
        }
        return this.userInRole;
    }

    /**
     * Gets the value of the localMixture property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localMixture property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalMixture().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001LocalMixture }
     * 
     * 
     */
    public List<OpenHR001LocalMixture> getLocalMixture() {
        if (localMixture == null) {
            localMixture = new ArrayList<OpenHR001LocalMixture>();
        }
        return this.localMixture;
    }

    /**
     * Gets the value of the auditTrail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the auditTrail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuditTrail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001AuditTrail }
     * 
     * 
     */
    public List<OpenHR001AuditTrail> getAuditTrail() {
        if (auditTrail == null) {
            auditTrail = new ArrayList<OpenHR001AuditTrail>();
        }
        return this.auditTrail;
    }

    /**
     * Gets the value of the confidentialityPolicy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the confidentialityPolicy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConfidentialityPolicy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001ConfidentialityPolicy }
     * 
     * 
     */
    public List<OpenHR001ConfidentialityPolicy> getConfidentialityPolicy() {
        if (confidentialityPolicy == null) {
            confidentialityPolicy = new ArrayList<OpenHR001ConfidentialityPolicy>();
        }
        return this.confidentialityPolicy;
    }

}
