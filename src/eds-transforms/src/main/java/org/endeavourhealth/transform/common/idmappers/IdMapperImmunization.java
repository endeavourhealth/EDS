package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperImmunization extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Immunization immunization = (Immunization)resource;

        super.mapResourceId(immunization, serviceId, systemId);
        super.mapExtensions(immunization, serviceId, systemId);

        if (immunization.hasIdentifier()) {
            super.mapIdentifiers(immunization.getIdentifier(), serviceId, systemId);
        }
        if (immunization.hasPatient()) {
            super.mapReference(immunization.getPatient(), serviceId, systemId);
        }
        if (immunization.hasPerformer()) {
            super.mapReference(immunization.getPerformer(), serviceId, systemId);
        }
        if (immunization.hasRequester()) {
            super.mapReference(immunization.getRequester(), serviceId, systemId);
        }
        if (immunization.hasEncounter()) {
            super.mapReference(immunization.getEncounter(), serviceId, systemId);
        }
        if (immunization.hasManufacturer()) {
            super.mapReference(immunization.getManufacturer(), serviceId, systemId);
        }
        if (immunization.hasLocation()) {
            super.mapReference(immunization.getLocation(), serviceId, systemId);
        }
        if (immunization.hasReaction()) {
            for (Immunization.ImmunizationReactionComponent reaction: immunization.getReaction()) {
                if (reaction.hasDetail()) {
                    super.mapReference(reaction.getDetail(), serviceId, systemId);
                }
            }
        }
        if (immunization.hasVaccinationProtocol()) {
            for (Immunization.ImmunizationVaccinationProtocolComponent protocol: immunization.getVaccinationProtocol()) {
                if (protocol.hasAuthority()) {
                    super.mapReference(protocol.getAuthority(), serviceId, systemId);
                }
            }
        }

    }
}
