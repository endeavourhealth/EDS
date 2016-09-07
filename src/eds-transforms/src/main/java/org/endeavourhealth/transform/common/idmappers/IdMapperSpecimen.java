package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Specimen;

import java.util.UUID;

public class IdMapperSpecimen extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Specimen specimen = (Specimen)resource;

        super.mapResourceId(specimen, serviceId, systemId);
        super.mapExtensions(specimen, serviceId, systemId);

        if (specimen.hasIdentifier()) {
            super.mapIdentifiers(specimen.getIdentifier(), resource, serviceId, systemId);
        }
        if (specimen.hasParent()) {
            super.mapReference(specimen.getSubject(), resource, serviceId, systemId);
        }
        if (specimen.hasSubject()) {
            super.mapReference(specimen.getSubject(), resource, serviceId, systemId);
        }
        if (specimen.hasCollection()) {
            if (specimen.getCollection().hasCollector()) {
                super.mapReference(specimen.getCollection().getCollector(), resource, serviceId, systemId);
            }
        }
        if (specimen.hasTreatment()) {
            for (Specimen.SpecimenTreatmentComponent treatment: specimen.getTreatment()) {
                if (treatment.hasAdditive()) {
                    super.mapReferences(treatment.getAdditive(), resource, serviceId, systemId);
                }
            }
        }
        if (specimen.hasContainer()) {
            for (Specimen.SpecimenContainerComponent container: specimen.getContainer()) {
                if (container.hasAdditive()) {
                    try {
                        super.mapReference(container.getAdditiveReference(), resource, serviceId, systemId);
                    } catch (Exception ex) {
                        //do nothing if not a reference
                    }
                }
            }
        }
    }
}
