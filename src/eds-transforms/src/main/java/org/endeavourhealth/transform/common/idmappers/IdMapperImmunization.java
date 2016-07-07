package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperImmunization extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Immunization immunization = (Immunization)resource;

        super.mapResourceId(immunization, serviceId, systemInstanceId);
        super.mapExtensions(immunization, serviceId, systemInstanceId);

        if (immunization.hasIdentifier()) {
            super.mapIdentifiers(immunization.getIdentifier(), serviceId, systemInstanceId);
        }
        if (immunization.hasPatient()) {
            super.mapReference(immunization.getPatient(), serviceId, systemInstanceId);
        }
        if (immunization.hasPerformer()) {
            super.mapReference(immunization.getPerformer(), serviceId, systemInstanceId);
        }
        if (immunization.hasRequester()) {
            super.mapReference(immunization.getRequester(), serviceId, systemInstanceId);
        }
        if (immunization.hasEncounter()) {
            super.mapReference(immunization.getEncounter(), serviceId, systemInstanceId);
        }
        if (immunization.hasManufacturer()) {
            super.mapReference(immunization.getManufacturer(), serviceId, systemInstanceId);
        }
        if (immunization.hasLocation()) {
            super.mapReference(immunization.getLocation(), serviceId, systemInstanceId);
        }
        if (immunization.hasReaction()) {
            for (Immunization.ImmunizationReactionComponent reaction: immunization.getReaction()) {
                if (reaction.hasDetail()) {
                    super.mapReference(reaction.getDetail(), serviceId, systemInstanceId);
                }
            }
        }
        if (immunization.hasVaccinationProtocol()) {
            for (Immunization.ImmunizationVaccinationProtocolComponent protocol: immunization.getVaccinationProtocol()) {
                if (protocol.hasAuthority()) {
                    super.mapReference(protocol.getAuthority(), serviceId, systemInstanceId);
                }
            }
        }

    }
}
