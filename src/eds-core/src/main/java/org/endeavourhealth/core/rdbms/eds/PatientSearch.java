package org.endeavourhealth.core.rdbms.eds;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
//@IdClass(PatientSearchPK.class)
@Table(name = "patient_search", schema = "public", catalog = "eds")
public class PatientSearch implements Serializable {

    private String serviceId = null;
    private String systemId = null;
    private String nhsNumber = null;
    private String forenames = null;
    private String surname = null;
    private Date dateOfBirth = null;
    private String postcode = null;
    private String gender = null;
    private Date registrationStart = null;
    private Date registrationEnd = null;
    private String patientId = null;
    private Date lastUpdated = null;

    @Id
    //@Type(type="pg-uuid")
    //@Type(type="uuid-char")
    //@Column(name="service_id", columnDefinition="uuid", nullable = false)
    @Column(name = "service_id", nullable = false)
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    //@Type(type="pg-uuid")
    //@Type(type="uuid-char")
    //@Column(name = "system_id", columnDefinition="uuid", nullable = false)
    @Column(name = "system_id", nullable = false)
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

   // @Basic
    @Column(name = "nhs_number")
    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

  //  @Basic
    @Column(name = "forenames")
    public String getForenames() {
        return forenames;
    }

    public void setForenames(String forenames) {
        this.forenames = forenames;
    }

   // @Basic
    @Column(name = "surname")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

  //  @Basic
    @Column(name = "date_of_birth")
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

//    @Basic
    @Column(name = "postcode")
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

 //   @Basic
    @Column(name = "gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

 //   @Basic
    @Column(name = "registration_start")
    public Date getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

  //  @Basic
    @Column(name = "registration_end")
    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    @Id
    //@Type(type="pg-uuid")
    //@Type(type="uuid-char")
    @Column(name = "patient_id", nullable = false)
    //@Column(name = "patient_id", columnDefinition="uuid", nullable = false)
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

  //  @Basic
    @Column(name = "last_updated", nullable = false)
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


}
