package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;
import java.util.UUID;

public class AllergyIntoleranceTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.AllergyIntolerance model = new org.endeavourhealth.transform.enterprise.schema.AllergyIntolerance();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            AllergyIntolerance fhir = (AllergyIntolerance)deserialiseResouce(resource);

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

            if (fhir.hasRecorder()) {
                Reference practitionerReference = fhir.getRecorder();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasOnset()) {
                DateTimeType dt = fhir.getOnsetElement();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));

            }

            Long snomedConceptId = findSnomedConceptId(fhir.getSubstance());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getAllergyIntolerance().add(model);
    }


}

