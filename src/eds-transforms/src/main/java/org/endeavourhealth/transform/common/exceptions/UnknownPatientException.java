package org.endeavourhealth.transform.common.exceptions;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class UnknownPatientException extends TransformException {

    public UnknownPatientException(String id, ResourceType resourceType, String resourceId, UUID serviceId, UUID systemId) {
        super("Reference to unrecognised patient " + id + " in " + resourceType + " " + resourceId + " for service " + serviceId + " and system " + systemId);
    }
}
