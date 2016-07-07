package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Order order = (Order)resource;

        super.mapResourceId(order, serviceId, systemInstanceId);
        super.mapExtensions(order, serviceId, systemInstanceId);

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), serviceId, systemInstanceId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), serviceId, systemInstanceId);
        }
        if (order.hasSource()) {
            super.mapReference(order.getSource(), serviceId, systemInstanceId);
        }
        if (order.hasTarget()) {
            super.mapReference(order.getTarget(), serviceId, systemInstanceId);
        }
        if (order.hasReason()) {
            try {
                super.mapReference(order.getReasonReference(), serviceId, systemInstanceId);
            } catch (Exception ex) {
                //not a problem if not a reference
            }
        }
        if (order.hasDetail()) {
            super.mapReferences(order.getDetail(), serviceId, systemInstanceId);
        }
    }
}
