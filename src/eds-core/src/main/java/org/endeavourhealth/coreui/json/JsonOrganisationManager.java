package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonOrganisationManager {
    private String name = null;
    private String alternativeName = null;
    private String odsCode = null;
    private String icoCode = null;
    private String igToolkitStatus = null;
    private String dateOfRegistration = null;
    private String registrationPerson = null;
    private String evidenceOfRegistration = null;
    private String postcode = null;
    private String geolocation = null;
    private String uuid = null;
    private Map<UUID, String> regions = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Map<UUID, String> getRegions() {
        return regions;
    }

    public void setRegions(Map<UUID, String> regions) {
        this.regions = regions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }

    public String getIcoCode() {
        return icoCode;
    }

    public void setIcoCode(String icoCode) {
        this.icoCode = icoCode;
    }

    public String getIgToolkitStatus() {
        return igToolkitStatus;
    }

    public void setIgToolkitStatus(String igToolkitStatus) {
        this.igToolkitStatus = igToolkitStatus;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getRegistrationPerson() {
        return registrationPerson;
    }

    public void setRegistrationPerson(String registrationPerson) {
        this.registrationPerson = registrationPerson;
    }

    public String getEvidenceOfRegistration() {
        return evidenceOfRegistration;
    }

    public void setEvidenceOfRegistration(String evidenceOfRegistration) {
        this.evidenceOfRegistration = evidenceOfRegistration;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }
}