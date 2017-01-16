package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.*;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConditionTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        //org.endeavourhealth.core.xml.enterprise.Condition model = new org.endeavourhealth.core.xml.enterprise.Condition();
        org.endeavourhealth.core.xml.enterprise.Observation model = new org.endeavourhealth.core.xml.enterprise.Observation();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Condition fhir = (Condition)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(new Patient(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(new Encounter(), encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasAsserter()) {
                Reference practitionerReference = fhir.getAsserter();
                Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasOnsetDateTimeType()) {
                DateTimeType dt = fhir.getOnsetDateTimeType();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));

            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);

            //if it's a problem set the boolean to say so
            if (fhir.hasMeta()) {
                for (UriType uriType: fhir.getMeta().getProfile()) {
                    if (uriType.getValue().equals(FhirUri.PROFILE_URI_PROBLEM)) {
                        model.setIsProblem(true);
                    }
                }
            }

            //if a condition is part of a problem but has the same code as the problem itself,
            //then we know it's a review, since EMIS re-records the diagnostic code for reviews
/*            model.setIsReview(new Boolean(false));

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
            }*/

            //add the raw original code, to assist in data checking
            String originalCode = findOriginalCode(fhir.getCode());
            model.setOriginalCode(originalCode);

            //add original term too, for easy display of results
            String originalTerm = fhir.getCode().getText();
            model.setOriginalTerm(originalTerm);
        }

        data.getObservation().add(model);
    }


}
