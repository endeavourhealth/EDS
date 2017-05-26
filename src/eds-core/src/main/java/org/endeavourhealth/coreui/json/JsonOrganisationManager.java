package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;

import java.util.*;

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
    private String uuid = null;
    private String IsService = null;
    private String bulkImported = null;
    private String bulkItemUpdated = null;
    private String bulkConflictedWith = null;
    private Map<UUID, String> regions = null;
    private Map<UUID, String> parentOrganisations = null;
    private Map<UUID, String> childOrganisations = null;
    private Map<UUID, String> services = null;
    private List<JsonAddress> addresses = new ArrayList<>();

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

    public List<JsonAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<JsonAddress> addresses) {
        this.addresses = addresses;
    }

    public String getIsService() {
        return IsService;
    }

    public void setIsService(String isService) {
        IsService = isService;
    }

    public Map<UUID, String> getParentOrganisations() {
        return parentOrganisations;
    }

    public void setParentOrganisations(Map<UUID, String> parentOrganisations) {
        this.parentOrganisations = parentOrganisations;
    }

    public Map<UUID, String> getChildOrganisations() {
        return childOrganisations;
    }

    public void setChildOrganisations(Map<UUID, String> childOrganisations) {
        this.childOrganisations = childOrganisations;
    }

    public Map<UUID, String> getServices() {
        return services;
    }

    public void setServices(Map<UUID, String> services) {
        this.services = services;
    }

    public String getBulkImported() {
        return bulkImported;
    }

    public void setBulkImported(String bulkImported) {
        this.bulkImported = bulkImported;
    }

    public String getBulkItemUpdated() {
        return bulkItemUpdated;
    }

    public void setBulkItemUpdated(String bulkItemUpdated) {
        this.bulkItemUpdated = bulkItemUpdated;
    }

    public String getBulkConflictedWith() {
        return bulkConflictedWith;
    }

    public void setBulkConflictedWith(String bulkConflictedWith) {
        this.bulkConflictedWith = bulkConflictedWith;
    }
}