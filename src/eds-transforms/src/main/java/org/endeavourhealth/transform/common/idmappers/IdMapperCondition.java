package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperCondition extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Condition condition = (Condition)resource;

        if (condition.hasIdentifier()) {
            super.mapIdentifiers(condition.getIdentifier(), resource, serviceId, systemId);
        }
        if (condition.hasPatient()) {
            super.mapReference(condition.getPatient(), resource, serviceId, systemId);
        }
        if (condition.hasEncounter()) {
            super.mapReference(condition.getEncounter(), resource, serviceId, systemId);
        }
        if (condition.hasAsserter()) {
            super.mapReference(condition.getAsserter(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(condition, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Condition condition = (Condition)resource;
        if (condition.hasPatient()) {
            return ReferenceHelper.getReferenceId(condition.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
