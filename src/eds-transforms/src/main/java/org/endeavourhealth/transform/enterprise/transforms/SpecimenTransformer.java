package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Specimen;

import java.util.Map;

public class SpecimenTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Specimen model = new org.endeavourhealth.core.xml.enterprise.Specimen();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Specimen fhir = (Specimen)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getSubject();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                        Reference encounterReference = (Reference)extension.getValue();
                        Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                        model.setEncounterId(enterpriseEncounterUuid);

                    }
                }
            }

            if (fhir.hasCollection()) {

                Specimen.SpecimenCollectionComponent fhirCollection = fhir.getCollection();

                if (fhirCollection.hasCollectedDateTimeType()) {
                    DateTimeType dt = fhirCollection.getCollectedDateTimeType();
                    model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                    model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
                }

                if (fhirCollection.hasCollector()) {
                    Reference practitionerReference = fhirCollection.getCollector();
                    Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                    model.setPractitionerId(enterprisePractitionerUuid);
                }
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getSpecimen().add(model);
    }
}


