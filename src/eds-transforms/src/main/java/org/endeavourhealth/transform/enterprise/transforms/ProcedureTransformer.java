package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;
import java.util.UUID;

public class ProcedureTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Procedure model = new org.endeavourhealth.core.xml.enterprise.Procedure();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Procedure fhir = (Procedure)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getSubject();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasPerformer()) {
                if (fhir.getPerformer().size() > 1) {
                    throw new TransformException("Procedures with more than one performer not supported " + fhir.getId());
                }
                Procedure.ProcedurePerformerComponent performerComponent = fhir.getPerformer().get(0);
                Reference practitionerReference = performerComponent.getActor();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasPerformedDateTimeType()) {
                DateTimeType dt = fhir.getPerformedDateTimeType();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getProcedure().add(model);
    }


}

