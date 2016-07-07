package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperComposition extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Composition composition = (Composition)resource;

        super.mapResourceId(composition, serviceId, systemInstanceId);
        super.mapExtensions(composition, serviceId, systemInstanceId);

        if (composition.hasIdentifier()) {
            if (composition.getIdentifier().hasAssigner()) {
                super.mapReference(composition.getIdentifier().getAssigner(), serviceId, systemInstanceId);
            }
        }
        if (composition.hasSubject()) {
            super.mapReference(composition.getSubject(), serviceId, systemInstanceId);
        }
        if (composition.hasAuthor()) {
            super.mapReferences(composition.getAuthor(), serviceId, systemInstanceId);
        }
        if (composition.hasAttester()) {
            for (Composition.CompositionAttesterComponent attester: composition.getAttester()) {
                if (attester.hasParty()) {
                    super.mapReference(attester.getParty(), serviceId, systemInstanceId);
                }
            }
        }
        if (composition.hasCustodian()) {
            super.mapReference(composition.getCustodian(), serviceId, systemInstanceId);
        }
        if (composition.hasEvent()) {
            for (Composition.CompositionEventComponent event: composition.getEvent()) {
                if (event.hasDetail()) {
                    super.mapReferences(event.getDetail(), serviceId, systemInstanceId);
                }
            }
        }
        if (composition.hasEncounter()) {
            super.mapReference(composition.getEncounter(), serviceId, systemInstanceId);
        }
        if (composition.hasSection()) {
            for (Composition.SectionComponent section: composition.getSection()) {
                if (section.hasEntry()) {
                    super.mapReferences(section.getEntry(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
