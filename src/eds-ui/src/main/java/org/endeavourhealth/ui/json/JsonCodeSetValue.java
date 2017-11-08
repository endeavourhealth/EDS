package org.endeavourhealth.ui.json;

import org.endeavourhealth.core.database.dal.reference.models.Concept;

public final class JsonCodeSetValue {
    private String code;

    public JsonCodeSetValue(Concept conceptEntity) {
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
