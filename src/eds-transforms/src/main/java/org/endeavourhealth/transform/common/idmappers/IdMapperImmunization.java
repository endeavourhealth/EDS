package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperImmunization extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Immunization immunization = (Immunization)resource;

        if (immunization.hasIdentifier()) {
            super.mapIdentifiers(immunization.getIdentifier(), resource, serviceId, systemId);
        }
        if (immunization.hasPatient()) {
            super.mapReference(immunization.getPatient(), resource, serviceId, systemId);
        }
        if (immunization.hasPerformer()) {
            super.mapReference(immunization.getPerformer(), resource, serviceId, systemId);
        }
        if (immunization.hasRequester()) {
            super.mapReference(immunization.getRequester(), resource, serviceId, systemId);
        }
        if (immunization.hasEncounter()) {
            super.mapReference(immunization.getEncounter(), resource, serviceId, systemId);
        }
        if (immunization.hasManufacturer()) {
            super.mapReference(immunization.getManufacturer(), resource, serviceId, systemId);
        }
        if (immunization.hasLocation()) {
            super.mapReference(immunization.getLocation(), resource, serviceId, systemId);
        }
        if (immunization.hasReaction()) {
            for (Immunization.ImmunizationReactionComponent reaction: immunization.getReaction()) {
                if (reaction.hasDetail()) {
                    super.mapReference(reaction.getDetail(), resource, serviceId, systemId);
                }
            }
        }
        if (immunization.hasVaccinationProtocol()) {
            for (Immunization.ImmunizationVaccinationProtocolComponent protocol: immunization.getVaccinationProtocol()) {
                if (protocol.hasAuthority()) {
                    super.mapReference(protocol.getAuthority(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(immunization, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Immunization immunization = (Immunization)resource;
        if (immunization.hasPatient()) {
            return ReferenceHelper.getReferenceId(immunization.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
