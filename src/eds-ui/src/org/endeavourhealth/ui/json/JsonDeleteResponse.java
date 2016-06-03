package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonDeleteResponse {
    private boolean deletedOk = true;
    private List<String> validationFailures = null;

    public JsonDeleteResponse() {

    }

    public void addValidationFailure(String s) {
        if (validationFailures == null) {
            validationFailures = new ArrayList<>();
        }

        deletedOk = false;
        validationFailures.add(s);
    }
    public int size()
    {
        if (validationFailures == null) {
            return 0;
        } else {
            return validationFailures.size();
        }
    }


    /**
     * gets/sets
     */
    public Boolean getDeletedOk() {
        return deletedOk;
    }

    public void setDeletedOk(Boolean deletedOk) {
        this.deletedOk = deletedOk;
    }

    public List<String> getValidationFailures() {
        return validationFailures;
    }

    public void setValidationFailures(List<String> validationFailures) {
        this.validationFailures = validationFailures;
    }
}
