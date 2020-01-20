package org.endeavourhealth.ui.json;

import org.endeavourhealth.common.fhir.schema.OrganisationType;

public class JsonOrganisationType {
    private String code;
    private String description;

    public JsonOrganisationType(OrganisationType o) {
        this.code = o.getCode();
        this.description = o.getDescription();
    }

    public JsonOrganisationType() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
