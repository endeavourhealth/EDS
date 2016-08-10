package org.endeavourhealth.transform.common.exceptions;

import org.hl7.fhir.instance.model.Resource;

public class FieldNotEmptyException extends TransformException {

    private String fieldName = null;
    private String resourceType = null;
    private String resourceId = null;

    public FieldNotEmptyException(String fieldName, Resource resource) {
        this(fieldName, resource.getResourceType().toString(), resource.getId(), null);
    }

    public FieldNotEmptyException(String fieldName, String resourceType, String resourceId) {
        this(fieldName, resourceType, resourceId, null);
    }
    public FieldNotEmptyException(String fieldName, String resourceType, String resourceId, Throwable cause) {
        super("Expecting field " + fieldName + " to be empty when processing " + resourceType + " " + resourceId, cause);
        this.fieldName = fieldName;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
