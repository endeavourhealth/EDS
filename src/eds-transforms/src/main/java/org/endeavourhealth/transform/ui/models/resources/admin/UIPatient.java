package org.endeavourhealth.transform.ui.models.resources.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UIAddress;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIHumanName;
import org.endeavourhealth.transform.ui.models.types.UIInternalIdentifier;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPatient extends UIResource<UIPatient> {

    private UIInternalIdentifier patientId;
    private String nhsNumber;
    private UIHumanName name;
    private UIDate dateOfBirth;
    private String gender;
    private UIAddress homeAddress;

    public UIInternalIdentifier getPatientId() {
        return patientId;
    }

    public UIPatient setPatientId(UIInternalIdentifier patientId) {
        this.patientId = patientId;
        return this;
    }

    public UIHumanName getName() {
        return name;
    }

    public UIPatient setName(UIHumanName name) {
        this.name = name;
        return this;
    }

    public UIDate getDateOfBirth() {
        return dateOfBirth;
    }

    public UIPatient setDateOfBirth(UIDate dateOfBirth) {
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
