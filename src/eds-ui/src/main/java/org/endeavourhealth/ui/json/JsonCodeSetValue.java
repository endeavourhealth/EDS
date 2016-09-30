package org.endeavourhealth.ui.json;

import org.endeavourhealth.ui.database.models.ConceptEntity;

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
