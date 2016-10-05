package org.endeavourhealth.transform.ui.models.resources.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UIAddress;
import org.endeavourhealth.transform.ui.models.types.UIHumanName;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPatient extends UIResource<UIPatient> {

    private UUID serviceId;
    private UUID systemId;
    private UUID patientId;
    private String nhsNumber;
    private UIHumanName name;
    private Date dateOfBirth;
    private String gender;
    private UIAddress homeAddress;

    public UIHumanName getName() {
        return name;
    }

    public UIPatient setName(UIHumanName name) {
        this.name = name;
        return this;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public UIPatient setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public UIPatient setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UIPatient setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public UIPatient setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UIPatient setPatientId(UUID patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public UIPatient setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
        return this;
    }

    public UIAddress getHomeAddress() {
        return homeAddress;
    }

    public UIPatient setHomeAddress(UIAddress homeAddress) {
        this.homeAddress = homeAddress;
        return this;
    }
}
