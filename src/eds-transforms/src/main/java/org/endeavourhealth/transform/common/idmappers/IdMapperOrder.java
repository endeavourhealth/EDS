package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Order order = (Order)resource;

        super.mapCommonResourceFields(order, serviceId, systemId, mapResourceId);

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
    }
}
