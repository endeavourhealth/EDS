package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperDiagnosticOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        DiagnosticOrder order = (DiagnosticOrder)resource;

        super.mapResourceId(order, serviceId, systemInstanceId);
        super.mapExtensions(order, serviceId, systemInstanceId);

        if (order.hasIdentifier()) {
            super.mapIdentifiers(order.getIdentifier(), serviceId, systemInstanceId);
        }
        if (order.hasSubject()) {
            super.mapReference(order.getSubject(), serviceId, systemInstanceId);
        }
        if (order.hasOrderer()) {
            super.mapReference(order.getOrderer(), serviceId, systemInstanceId);
        }
        if (order.hasEncounter()) {
            super.mapReference(order.getEncounter(), serviceId, systemInstanceId);
        }
        if (order.hasSupportingInformation()) {
            super.mapReferences(order.getSupportingInformation(), serviceId, systemInstanceId);
        }
        if (order.hasSpecimen()) {
            super.mapReferences(order.getSpecimen(), serviceId, systemInstanceId);
        }
        if (order.hasEvent()) {
            for (DiagnosticOrder.DiagnosticOrderEventComponent event: order.getEvent()) {
                if (event.hasActor()) {
                    super.mapReference(event.getActor(), serviceId, systemInstanceId);
                }
            }
        }
        if (order.hasItem()) {
            for (DiagnosticOrder.DiagnosticOrderItemComponent item: order.getItem()) {
                if (item.hasSpecimen()) {
                    super.mapReferences(item.getSpecimen(), serviceId, systemInstanceId);
                }
            }
        }

    }
}
