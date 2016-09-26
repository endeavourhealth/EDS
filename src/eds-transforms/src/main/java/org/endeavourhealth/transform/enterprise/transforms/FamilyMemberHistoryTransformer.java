package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;
import java.util.UUID;

public class FamilyMemberHistoryTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory model = new org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            FamilyMemberHistory fhir = (FamilyMemberHistory)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                        Reference encounterReference = (Reference)extension.getValue();
                        UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                        model.setEncounterId(enterpriseEncounterUuid.toString());
                    }
                }
            }

            //TODO - need to extract practitioner for FamilyMemberHistory
            /*if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }*/

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            if (fhir.getCondition().size() > 1) {
                throw new TransformException("DiagnosticOrder with more than one item not supported");
            }
            FamilyMemberHistory.FamilyMemberHistoryConditionComponent condition = fhir.getCondition().get(0);
            Long snomedConceptId = findSnomedConceptId(condition.getCode());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getFamilyMemberHistory().add(model);
    }


}

