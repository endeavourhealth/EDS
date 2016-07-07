package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Specimen;

import java.util.UUID;

public class IdMapperSpecimen extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Specimen specimen = (Specimen)resource;

        super.mapResourceId(specimen, serviceId, systemInstanceId);
        super.mapExtensions(specimen, serviceId, systemInstanceId);

        if (specimen.hasIdentifier()) {
            super.mapIdentifiers(specimen.getIdentifier(), serviceId, systemInstanceId);
        }
        if (specimen.hasParent()) {
            super.mapReference(specimen.getSubject(), serviceId, systemInstanceId);
        }
        if (specimen.hasSubject()) {
            super.mapReference(specimen.getSubject(), serviceId, systemInstanceId);
        }
        if (specimen.hasCollection()) {
            if (specimen.getCollection().hasCollector()) {
                super.mapReference(specimen.getCollection().getCollector(), serviceId, systemInstanceId);
            }
        }
        if (specimen.hasTreatment()) {
            for (Specimen.SpecimenTreatmentComponent treatment: specimen.getTreatment()) {
                if (treatment.hasAdditive()) {
                    super.mapReferences(treatment.getAdditive(), serviceId, systemInstanceId);
                }
            }
        }
        if (specimen.hasContainer()) {
            for (Specimen.SpecimenContainerComponent container: specimen.getContainer()) {
                if (container.hasAdditive()) {
                    try {
                        super.mapReference(container.getAdditiveReference(), serviceId, systemInstanceId);
                    } catch (Exception ex) {
                        //do nothing if not a reference
                    }
                }
            }
        }
    }
}
