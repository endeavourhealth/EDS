package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseIdMapper {

    /**
     * maps the main ID of any resource
     */
    protected void mapResourceId(Resource resource, UUID serviceId, UUID systemInstanceId) {

        String newId = IdHelper.getEdsResourceId(serviceId, systemInstanceId, resource.getResourceType(), resource.getId());
        resource.setId(newId);
    }

    /**
     * maps the IDs in any extensions of a resource
     */
    protected void mapExtensions(DomainResource resource, UUID serviceId, UUID systemInstanceId) {
        if (!resource.hasExtension()) {
            return;
        }

        for (Extension extension: resource.getExtension()) {
            if (extension.getValue() instanceof Reference) {
                mapReference((Reference)extension.getValue(), serviceId, systemInstanceId);
            }
        }
    }

    /**
     * maps the IDs in any identifiers of a resource
     */
    protected void mapIdentifiers(List<Identifier> identifiers, UUID serviceId, UUID systemInstanceId) {
        for (Identifier identifier: identifiers) {
            mapReference(identifier.getAssigner(), serviceId, systemInstanceId);
        }
    }

    /**
     * maps the ID within any reference
     */
    protected void mapReference(Reference reference, UUID serviceId, UUID systemInstanceId) {
        if (reference == null) {
            return;
        }

        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String newId = IdHelper.getEdsResourceId(serviceId, systemInstanceId, comps.getResourceType(), comps.getId());
        reference.setReference(ReferenceHelper.createResourceReference(comps.getResourceType(), newId));
    }

    /**
     * maps the ID within any reference
     */
    protected void mapReferences(List<Reference> references, UUID serviceId, UUID systemInstanceId) {
        if (references == null
                || references.isEmpty()) {
            return;
        }

        for (Reference reference: references) {
            mapReference(reference, serviceId, systemInstanceId);
        }
    }

    public abstract void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId);


}
