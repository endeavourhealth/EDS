package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperReferralRequest extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        ReferralRequest referralRequest = (ReferralRequest)resource;

        super.mapResourceId(referralRequest, serviceId, systemId);
        super.mapExtensions(referralRequest, serviceId, systemId);

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
    }
}
