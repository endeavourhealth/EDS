package org.endeavourhealth.patientexplorer.models;


import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;

public final class JsonCodeSetValue {
    private String code;

    public JsonCodeSetValue(ConceptEntity conceptEntity) {
        this.code =conceptEntity.getCode();
    }

    /**
     * gets/sets
     */

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
