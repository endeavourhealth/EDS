
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="AppointmentSession" type="{http://www.e-mis.com/emisopen/MedicalRecord}AppointmentSessionStruct" maxOccurs="unbounded" minOccurs="0"/>
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
    "appointmentSession"
})
@XmlRootElement(name = "AppointmentSessionList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
public class AppointmentSessionList {

    @XmlElement(name = "AppointmentSession", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<AppointmentSessionStruct> appointmentSession;

    /**
     * Gets the value of the appointmentSession property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appointmentSession property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppointmentSession().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AppointmentSessionStruct }
     * 
     * 
     */
    public List<AppointmentSessionStruct> getAppointmentSession() {
        if (appointmentSession == null) {
            appointmentSession = new ArrayList<AppointmentSessionStruct>();
        }
        return this.appointmentSession;
    }

}
