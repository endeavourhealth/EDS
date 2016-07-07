package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperReferralRequest extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        ReferralRequest referralRequest = (ReferralRequest)resource;

        super.mapResourceId(referralRequest, serviceId, systemInstanceId);
        super.mapExtensions(referralRequest, serviceId, systemInstanceId);

        if (referralRequest.hasIdentifier()) {
            super.mapIdentifiers(referralRequest.getIdentifier(), serviceId, systemInstanceId);
        }
        if (referralRequest.hasPatient()) {
            super.mapReference(referralRequest.getPatient(), serviceId, systemInstanceId);
        }
        if (referralRequest.hasRequester()) {
            super.mapReference(referralRequest.getRequester(), serviceId, systemInstanceId);
        }
        if (referralRequest.hasRecipient()) {
            super.mapReferences(referralRequest.getRecipient(), serviceId, systemInstanceId);
        }
        if (referralRequest.hasEncounter()) {
            super.mapReference(referralRequest.getEncounter(), serviceId, systemInstanceId);
        }
        if (referralRequest.hasSupportingInformation()) {
            super.mapReferences(referralRequest.getSupportingInformation(), serviceId, systemInstanceId);
        }
    }
}
