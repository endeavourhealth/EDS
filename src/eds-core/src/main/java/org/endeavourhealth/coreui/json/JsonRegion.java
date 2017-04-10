package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonRegion {
    private String name = null;
    private String description = null;
    private String uuid = null;
    private Integer organisationCount = null;
    private Map<UUID, String> organisations = null;
    private Map<UUID, String> parentRegions = null;
    private Map<UUID, String> childRegions = null;
    private Map<UUID, String> sharingAgreements = null;

    public Map<UUID, String> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Map<UUID, String> organisations) {
        this.organisations = organisations;
    }

    public Map<UUID, String> getParentRegions() {
        return parentRegions;
    }

    public void setParentRegions(Map<UUID, String> parentRegions) {
        this.parentRegions = parentRegions;
    }

    public Map<UUID, String> getChildRegions() {
        return childRegions;
    }

    public void setChildRegions(Map<UUID, String> childRegions) {
        this.childRegions = childRegions;
    }

    public JsonRegion() {
    }

    public JsonRegion(RegionEntity reg, Boolean isAdmin) {
        this.name = reg.getName();
        this.description = reg.getDescription();
        this.uuid = reg.getUuid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String Uuid) {
        this.uuid = Uuid;
    }

    public Integer getOrganisationCount() {
        return this.organisationCount;
    }

    public void setOrganisationCount(Integer id) {
        this.organisationCount = id;
    }

    public Map<UUID, String> getSharingAgreements() {
        return sharingAgreements;
    }

    public void setSharingAgreements(Map<UUID, String> sharingAgreements) {
        this.sharingAgreements = sharingAgreements;
    }
}