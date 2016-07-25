package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperDiagnosticOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        DiagnosticOrder order = (DiagnosticOrder)resource;

        super.mapResourceId(order, serviceId, systemId);
        super.mapExtensions(order, serviceId, systemId);

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), serviceId, systemId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), serviceId, systemId);
        }
        if (order.hasOrderer()) {
            super.mapReference(order.getOrderer(), serviceId, systemId);
        }
        if (order.hasEncounter()) {
            super.mapReference(order.getEncounter(), serviceId, systemId);
        }
        if (order.hasSupportingInformation()) {
            super.mapReferences(order.getSupportingInformation(), serviceId, systemId);
        }
        if (order.hasSpecimen()) {
            super.mapReferences(order.getSpecimen(), serviceId, systemId);
        }
        if (order.hasEvent()) {
            for (DiagnosticOrder.DiagnosticOrderEventComponent event: order.getEvent()) {
                if (event.hasActor()) {
                    super.mapReference(event.getActor(), serviceId, systemId);
                }
            }
        }
        if (order.hasItem()) {
            for (DiagnosticOrder.DiagnosticOrderItemComponent item: order.getItem()) {
                if (item.hasSpecimen()) {
                    super.mapReferences(item.getSpecimen(), serviceId, systemId);
                }
            }
        }

    }
}
