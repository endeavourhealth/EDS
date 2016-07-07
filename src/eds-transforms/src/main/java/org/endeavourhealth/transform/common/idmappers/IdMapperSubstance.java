package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Substance;

import java.util.UUID;

public class IdMapperSubstance extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Substance substance = (Substance)resource;

        super.mapResourceId(substance, serviceId, systemInstanceId);
        super.mapExtensions(substance, serviceId, systemInstanceId);

        if (substance.hasIdentifier()) {
            super.mapIdentifiers(substance.getIdentifier(), serviceId, systemInstanceId);
        }
        if (substance.hasInstance()) {
            for (Substance.SubstanceInstanceComponent instance: substance.getInstance()) {
                if (instance.hasIdentifier()) {
                    if (instance.getIdentifier().hasAssigner()) {
                        super.mapReference(instance.getIdentifier().getAssigner(), serviceId, systemInstanceId);
                    }
                }
            }
        }
        if (substance.hasIngredient()) {
            for (Substance.SubstanceIngredientComponent ingredient: substance.getIngredient()) {
                if (ingredient.hasSubstance()) {
                    super.mapReference(ingredient.getSubstance(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
