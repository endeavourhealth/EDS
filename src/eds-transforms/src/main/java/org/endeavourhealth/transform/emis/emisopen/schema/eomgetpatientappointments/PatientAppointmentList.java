
package org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments;

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
 *         &lt;element name="Appointment" type="{http://www.e-mis.com/emisopen/MedicalRecord}AppointmentStruct" maxOccurs="unbounded" minOccurs="0"/>
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
    "appointment"
})
@XmlRootElement(name = "PatientAppointmentList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
public class PatientAppointmentList {

    @XmlElement(name = "Appointment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<AppointmentStruct> appointment;

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
     * {@link AppointmentStruct }
     * 
     * 
     */
    public List<AppointmentStruct> getAppointment() {
        if (appointment == null) {
            appointment = new ArrayList<AppointmentStruct>();
        }
        return this.appointment;
    }

}
