package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonResourceContainer {

    private String resourceId = null;
    private String resourceType = null;
    private String resourceJson = null;

    public JsonResourceContainer() {}

    public JsonResourceContainer(ResourceWrapper resourceHistory) {
        this.resourceId = resourceHistory.getResourceId().toString();
        this.resourceType = resourceHistory.getResourceType();
        this.resourceJson = resourceHistory.getResourceData();
    }


    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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
