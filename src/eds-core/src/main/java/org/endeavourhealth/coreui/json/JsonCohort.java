package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonCohort {
    private String uuid = null;
    private String name = null;
    private String nature = null;
    private String patientCohortInclusionConsentModel = null;
    private String queryDefinition = null;
    private String removalPolicy = null;
    private Map<UUID, String> dpas = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getPatientCohortInclusionConsentModel() {
        return patientCohortInclusionConsentModel;
    }

    public void setPatientCohortInclusionConsentModel(String patientCohortInclusionConsentModel) {
        this.patientCohortInclusionConsentModel = patientCohortInclusionConsentModel;
    }

    public String getQueryDefinition() {
        return queryDefinition;
    }

    public void setQueryDefinition(String queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    public String getRemovalPolicy() {
        return removalPolicy;
    }

    public void setRemovalPolicy(String removalPolicy) {
        this.removalPolicy = removalPolicy;
    }

    public Map<UUID, String> getDpas() {
        return dpas;
    }

    public void setDpas(Map<UUID, String> dpas) {
        this.dpas = dpas;
    }
}
