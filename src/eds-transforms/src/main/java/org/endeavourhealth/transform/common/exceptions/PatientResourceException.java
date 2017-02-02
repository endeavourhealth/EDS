package org.endeavourhealth.transform.common.exceptions;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

public class PatientResourceException extends TransformException {

    private ResourceType resourceType = null;
    private String resourceId = null;
    private boolean expectingPatientType = false;

    public PatientResourceException(Resource resource, boolean expectingPatientType) {
        this(resource, expectingPatientType, null);
    }

    public PatientResourceException(Resource resource, boolean expectingPatientType, Throwable cause) {
        super("Trying to treat " + resource.getResourceType() + " " + resource.getId() + " as " + (expectingPatientType ? "patient" : "non-patient") + " resource", cause);
        this.resourceType = resource.getResourceType();
        this.resourceId = resource.getId();
        this.expectingPatientType = expectingPatientType;
    }
    /*public PatientResourceException(ResourceType resourceType, boolean expectingPatientType) {
        this(resourceType, expectingPatientType, null);
    }
    public PatientResourceException(ResourceType resourceType, boolean expectingPatientType, Throwable cause) {
        super("Trying to treat " + resourceType + " resource as " + (expectingPatientType ? "patient" : "non-patient") + " resource", cause);
        this.resourceType = resourceType;
        this.expectingPatientType = expectingPatientType;
    }*/

    public ResourceType getResourceType() {
        return resourceType;
    }

    public boolean isExpectingPatientType() {
        return expectingPatientType;
    }
}
