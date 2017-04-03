package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperOrder extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Order order = (Order)resource;

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), resource, serviceId, systemId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), resource, serviceId, systemId);
        }
        if (order.hasSource()) {
            super.mapReference(order.getSource(), resource, serviceId, systemId);
        }
        if (order.hasTarget()) {
            super.mapReference(order.getTarget(), resource, serviceId, systemId);
        }
        if (order.hasReason()) {
            try {
                super.mapReference(order.getReasonReference(), resource, serviceId, systemId);
            } catch (Exception ex) {
                //not a problem if not a reference
            }
        }
        if (order.hasDetail()) {
            super.mapReferences(order.getDetail(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(order, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Order order = (Order)resource;
        if (order.hasSubject()) {
            return ReferenceHelper.getReferenceId(order.getSubject(), ResourceType.Patient);
        }
        return null;
    }
}
