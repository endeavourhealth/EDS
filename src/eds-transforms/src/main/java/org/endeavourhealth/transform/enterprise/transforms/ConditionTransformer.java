package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;

public class ConditionTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Condition model = new org.endeavourhealth.core.xml.enterprise.Condition();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Condition fhir = (Condition)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasAsserter()) {
                Reference practitionerReference = fhir.getAsserter();
                Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasOnsetDateTimeType()) {
                DateTimeType dt = fhir.getOnsetDateTimeType();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));

            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);

            //if a condition is part of a problem but has the same code as the problem itself,
            //then we know it's a review, since EMIS re-records the diagnostic code for reviews
            model.setIsReview(new Boolean(false));

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.CONDITION_PART_OF_PROBLEM)) {
                        Reference problemReference = (Reference)extension.getValue();
                        Condition fhirProblem = (Condition)findResource(problemReference, otherResources);
                        if (fhirProblem != null) {

                            Long problemSnomedConceptId = findSnomedConceptId(fhirProblem.getCode());
                            if (snomedConceptId.equals(problemSnomedConceptId)) {
                                model.setIsReview(new Boolean(true));
                            }
                        }
                    }
                }
            }

            //add the raw original code, to assist in data checking
            String originalCode = findOriginalCode(fhir.getCode());
            model.setOriginalCode(originalCode);
        }

        data.getCondition().add(model);
    }


}
