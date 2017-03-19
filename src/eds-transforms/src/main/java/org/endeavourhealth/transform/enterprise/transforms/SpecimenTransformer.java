package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
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
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Observation model = data.getObservations();

        Long enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.longValue());

        } else {

            Specimen fhir = (Specimen)deserialiseResouce(resource);

            /*Reference patientReference = fhir.getSubject();
            Long enterprisePatientId = findEnterpriseId(data.getPatients(), patientReference);*/

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientId == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            long id;
            long organisationId;
            long patientId;
            long personId;
            Long encounterId = null;
            Long practitionerId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long snomedConceptId = null;
            BigDecimal value = null;
            String units = null;
            String originalCode = null;
            boolean isProblem = false;
            String originalTerm = null;

            id = enterpriseId.longValue();
            organisationId = enterpriseOrganisationId.longValue();
            patientId = enterprisePatientId.longValue();
            personId = enterprisePersonId.longValue();

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

            snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhir.getType());

            //add the raw original code, to assist in data checking
            originalCode = CodeableConceptHelper.findOriginalCode(fhir.getType());

            //add original term too, for easy display of results
            originalTerm = fhir.getType().getText();

            model.writeUpsert(id,
                    organisationId,
                    patientId,
                    personId,
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
}


