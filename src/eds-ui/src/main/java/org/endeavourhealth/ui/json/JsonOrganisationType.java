package org.endeavourhealth.ui.json;

import org.endeavourhealth.common.fhir.schema.OrganisationType;

public class JsonOrganisationType {
    private String code;
    private String description;
    private String name;

    public JsonOrganisationType(OrganisationType o) {
        this.code = o.getCode();
        this.description = o.getDescription();
        this.name = o.toString(); //the name of the enum e.g. GP_PRACTICE
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
