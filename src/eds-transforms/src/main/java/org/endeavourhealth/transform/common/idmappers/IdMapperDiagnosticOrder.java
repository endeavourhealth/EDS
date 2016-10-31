package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperDiagnosticOrder extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        DiagnosticOrder order = (DiagnosticOrder)resource;

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), resource, serviceId, systemId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), resource, serviceId, systemId);
        }
        if (order.hasOrderer()) {
            super.mapReference(order.getOrderer(), resource, serviceId, systemId);
        }
        if (order.hasEncounter()) {
            super.mapReference(order.getEncounter(), resource, serviceId, systemId);
        }
        if (order.hasSupportingInformation()) {
            super.mapReferences(order.getSupportingInformation(), resource, serviceId, systemId);
        }
        if (order.hasSpecimen()) {
            super.mapReferences(order.getSpecimen(), resource, serviceId, systemId);
        }
        if (order.hasEvent()) {
            for (DiagnosticOrder.DiagnosticOrderEventComponent event: order.getEvent()) {
                if (event.hasActor()) {
                    super.mapReference(event.getActor(), resource, serviceId, systemId);
                }
            }
        }
        if (order.hasItem()) {
            for (DiagnosticOrder.DiagnosticOrderItemComponent item: order.getItem()) {
                if (item.hasSpecimen()) {
                    super.mapReferences(item.getSpecimen(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(order, serviceId, systemId, mapResourceId);
    }
}
