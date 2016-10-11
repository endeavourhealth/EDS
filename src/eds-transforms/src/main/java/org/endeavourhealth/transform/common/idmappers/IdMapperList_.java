package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.List_;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperList_ extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        List_ list = (List_)resource;

        super.mapResourceId(list, serviceId, systemId);
        super.mapExtensions(list, serviceId, systemId);

        if (list.hasIdentifier()) {
            super.mapIdentifiers(list.getIdentifier(), resource, serviceId, systemId);
        }
        if (list.hasSubject()) {
            super.mapReference(list.getSubject(), resource, serviceId, systemId);
        }
        if (list.hasSource()) {
            super.mapReference(list.getSource(), resource, serviceId, systemId);
        }
        if (list.hasEncounter()) {
            super.mapReference(list.getEncounter(), resource, serviceId, systemId);
        }
        if (list.hasEntry()) {
            for (List_.ListEntryComponent entry: list.getEntry()) {
                if (entry.hasItem()) {
                    super.mapReference(entry.getItem(), resource, serviceId, systemId);
                }
            }
        }

    }
}
