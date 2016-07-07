package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperCondition extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Condition condition = (Condition)resource;

        super.mapResourceId(condition, serviceId, systemInstanceId);
        super.mapExtensions(condition, serviceId, systemInstanceId);

        if (condition.hasIdentifier()) {
            super.mapIdentifiers(condition.getIdentifier(), serviceId, systemInstanceId);
        }
        if (condition.hasPatient()) {
            super.mapReference(condition.getPatient(), serviceId, systemInstanceId);
        }
        if (condition.hasEncounter()) {
            super.mapReference(condition.getEncounter(), serviceId, systemInstanceId);
        }
        if (condition.hasAsserter()) {
            super.mapReference(condition.getAsserter(), serviceId, systemInstanceId);
        }



    }
}
