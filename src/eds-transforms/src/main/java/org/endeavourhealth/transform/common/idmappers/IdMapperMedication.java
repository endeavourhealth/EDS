package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperMedication extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Medication medication = (Medication)resource;

        super.mapResourceId(medication, serviceId, systemInstanceId);
        super.mapExtensions(medication, serviceId, systemInstanceId);

        if (medication.hasManufacturer()) {
            super.mapReference(medication.getManufacturer(), serviceId, systemInstanceId);
        }
        if (medication.hasProduct()) {
            for (Medication.MedicationProductIngredientComponent ingredient: medication.getProduct().getIngredient()) {
                if (ingredient.hasItem()) {
                    super.mapReference(ingredient.getItem(), serviceId, systemInstanceId);
                }
            }
        }

        if (medication.hasPackage()) {
            for (Medication.MedicationPackageContentComponent content: medication.getPackage().getContent()) {
                if (content.hasItem()) {
                    super.mapReference(content.getItem(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
