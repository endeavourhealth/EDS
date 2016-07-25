package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Order order = (Order)resource;

        super.mapResourceId(order, serviceId, systemId);
        super.mapExtensions(order, serviceId, systemId);

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), serviceId, systemId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), serviceId, systemId);
        }
        if (order.hasSource()) {
            super.mapReference(order.getSource(), serviceId, systemId);
        }
        if (order.hasTarget()) {
            super.mapReference(order.getTarget(), serviceId, systemId);
        }
        if (order.hasReason()) {
            try {
                super.mapReference(order.getReasonReference(), serviceId, systemId);
            } catch (Exception ex) {
                //not a problem if not a reference
            }
        }
        if (order.hasDetail()) {
            super.mapReferences(order.getDetail(), serviceId, systemId);
        }
    }
}
