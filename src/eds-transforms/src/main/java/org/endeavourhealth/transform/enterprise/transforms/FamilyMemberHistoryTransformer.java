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

public class FamilyMemberHistoryTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory model = new org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            FamilyMemberHistory fhir = (FamilyMemberHistory)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                        Reference encounterReference = (Reference)extension.getValue();
                        Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                        model.setEncounterId(enterpriseEncounterUuid);

                    } else if (extension.getUrl().equals(FhirExtensionUri.FAMILY_MEMBER_HISTOY_REPORTED_BY)) {
                        Reference practitionerReference = (Reference)extension.getValue();
                        Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            if (fhir.getCondition().size() > 1) {
                throw new TransformException("FamilyMemberHistory with more than one item not supported");
            }
            FamilyMemberHistory.FamilyMemberHistoryConditionComponent condition = fhir.getCondition().get(0);
            Long snomedConceptId = findSnomedConceptId(condition.getCode());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getFamilyMemberHistory().add(model);
    }


}

