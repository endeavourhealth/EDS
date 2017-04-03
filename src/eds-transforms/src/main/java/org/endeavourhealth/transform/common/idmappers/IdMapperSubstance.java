package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Substance;

import java.util.UUID;

public class IdMapperSubstance extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Substance substance = (Substance)resource;

        if (substance.hasIdentifier()) {
            super.mapIdentifiers(substance.getIdentifier(), resource, serviceId, systemId);
        }
        if (substance.hasInstance()) {
            for (Substance.SubstanceInstanceComponent instance: substance.getInstance()) {
                if (instance.hasIdentifier()) {
                    if (instance.getIdentifier().hasAssigner()) {
                        super.mapReference(instance.getIdentifier().getAssigner(), resource, serviceId, systemId);
                    }
                }
            }
        }
        if (substance.hasIngredient()) {
            for (Substance.SubstanceIngredientComponent ingredient: substance.getIngredient()) {
                if (ingredient.hasSubstance()) {
                    super.mapReference(ingredient.getSubstance(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(substance, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {
        throw new PatientResourceException(resource, true);
    }
}
