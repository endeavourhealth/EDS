package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonResourceContainer {

    private String resourceType = null;
    private String resourceJson = null;

    public JsonResourceContainer() {}

    public JsonResourceContainer(ResourceHistory resourceHistory) {
        this.resourceType = resourceHistory.getResourceType();
        this.resourceJson = resourceHistory.getResourceData();
    }

    public JsonResourceContainer(ResourceByPatient resourceHistory) {
        this.resourceType = resourceHistory.getResourceType();
        this.resourceJson = resourceHistory.getResourceData();
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceJson() {
        return resourceJson;
    }

    public void setResourceJson(String resourceJson) {
        this.resourceJson = resourceJson;
    }

}
