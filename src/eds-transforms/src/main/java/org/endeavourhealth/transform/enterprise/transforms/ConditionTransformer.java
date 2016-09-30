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
import java.util.UUID;

public class ConditionTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Condition model = new org.endeavourhealth.core.xml.enterprise.Condition();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Condition fhir = (Condition)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasAsserter()) {
                Reference practitionerReference = fhir.getAsserter();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasOnsetDateTimeType()) {
                DateTimeType dt = fhir.getOnsetDateTimeType();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));

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
        }

        data.getCondition().add(model);
    }


}
