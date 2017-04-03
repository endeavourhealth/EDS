package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperReferralRequest extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        ReferralRequest referralRequest = (ReferralRequest)resource;

        if (referralRequest.hasIdentifier()) {
            super.mapIdentifiers(referralRequest.getIdentifier(), resource, serviceId, systemId);
        }
        if (referralRequest.hasPatient()) {
            super.mapReference(referralRequest.getPatient(), resource, serviceId, systemId);
        }
        if (referralRequest.hasRequester()) {
            super.mapReference(referralRequest.getRequester(), resource, serviceId, systemId);
        }
        if (referralRequest.hasRecipient()) {
            super.mapReferences(referralRequest.getRecipient(), resource, serviceId, systemId);
        }
        if (referralRequest.hasEncounter()) {
            super.mapReference(referralRequest.getEncounter(), resource, serviceId, systemId);
        }
        if (referralRequest.hasSupportingInformation()) {
            super.mapReferences(referralRequest.getSupportingInformation(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(referralRequest, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) {

        ReferralRequest referralRequest = (ReferralRequest)resource;
        if (referralRequest.hasPatient()) {
            return ReferenceHelper.getReferenceId(referralRequest.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
