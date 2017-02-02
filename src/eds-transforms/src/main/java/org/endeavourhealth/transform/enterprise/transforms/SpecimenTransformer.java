package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class SpecimenTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(SpecimenTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Observation model = data.getObservations();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            Specimen fhir = (Specimen)deserialiseResouce(resource);

            Reference patientReference = fhir.getSubject();
            Integer enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            int id;
            int organisationId;
            int patientId;
            Integer encounterId = null;
            Integer practitionerId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long snomedConceptId = null;
            BigDecimal value = null;
            String units = null;
            String originalCode = null;
            boolean isProblem = false;
            String originalTerm = null;

            id = enterpriseId.intValue();
            organisationId = enterpriseOrganisationUuid.intValue();
            patientId = enterprisePatientUuid.intValue();

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                        Reference encounterReference = (Reference)extension.getValue();
                        encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
                    }
                }
            }

            if (fhir.hasCollection()) {

                Specimen.SpecimenCollectionComponent fhirCollection = fhir.getCollection();

                if (fhirCollection.hasCollectedDateTimeType()) {
                    DateTimeType dt = fhirCollection.getCollectedDateTimeType();
                    clinicalEffectiveDate = dt.getValue();
                    datePrecisionId = convertDatePrecision(dt.getPrecision());
                }

                if (fhirCollection.hasCollector()) {
                    Reference practitionerReference = fhirCollection.getCollector();
                    practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
                }
            }

            snomedConceptId = findSnomedConceptId(fhir.getType());

            //add the raw original code, to assist in data checking
            originalCode = findOriginalCode(fhir.getType());

            //add original term too, for easy display of results
            originalTerm = fhir.getType().getText();

            model.writeUpsert(id,
                    organisationId,
                    patientId,
                    encounterId,
                    practitionerId,
                    clinicalEffectiveDate,
                    datePrecisionId,
                    snomedConceptId,
                    value,
                    units,
                    originalCode,
                    isProblem,
                    originalTerm);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        //org.endeavourhealth.core.xml.enterprise.Specimen model = new org.endeavourhealth.core.xml.enterprise.Specimen();
        org.endeavourhealth.core.xml.enterprise.Observation model = new org.endeavourhealth.core.xml.enterprise.Observation();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Specimen fhir = (Specimen)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getSubject();
            Integer enterprisePatientUuid = findEnterpriseId(new Patient(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                        Reference encounterReference = (Reference)extension.getValue();
                        Integer enterpriseEncounterUuid = findEnterpriseId(new Encounter(), encounterReference);
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
                    Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                    model.setPractitionerId(enterprisePractitionerUuid);
                }
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);

            //add the raw original code, to assist in data checking
            String originalCode = findOriginalCode(fhir.getType());
            model.setOriginalCode(originalCode);

            //add original term too, for easy display of results
            String originalTerm = fhir.getType().getText();
            model.setOriginalTerm(originalTerm);
        }

        data.getObservation().add(model);
    }*/
}


