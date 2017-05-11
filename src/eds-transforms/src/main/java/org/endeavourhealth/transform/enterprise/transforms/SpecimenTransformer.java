package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class SpecimenTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(SpecimenTransformer.class);

    public boolean shouldAlwaysTransform() {
        return true;
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String enterpriseConfigName,
                          UUID protocolId) throws Exception {

        Specimen fhir = (Specimen)resource;

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
        boolean isReview = false;

        id = enterpriseId.longValue();
        organisationId = enterpriseOrganisationId.longValue();
        patientId = enterprisePatientId.longValue();
        personId = enterprisePersonId.longValue();

        if (fhir.hasExtension()) {
            for (Extension extension: fhir.getExtension()) {
                if (extension.getUrl().equals(FhirExtensionUri.ASSOCIATED_ENCOUNTER)) {
                    Reference encounterReference = (Reference)extension.getValue();
                    encounterId = findEnterpriseId(enterpriseConfigName, encounterReference);
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
                practitionerId = findEnterpriseId(enterpriseConfigName, practitionerReference);
                if (practitionerId == null) {
                    practitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, enterpriseConfigName, protocolId);
                }
            }
        }

        snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhir.getType());

        //add the raw original code, to assist in data checking
        originalCode = CodeableConceptHelper.findOriginalCode(fhir.getType());

        //add original term too, for easy display of results
        originalTerm = fhir.getType().getText();

        Extension reviewExtension = ExtensionConverter.findExtension(fhir, FhirExtensionUri.IS_REVIEW);
        if (reviewExtension != null) {
            BooleanType b = (BooleanType)reviewExtension.getValue();
            if (b.getValue() != null) {
                isReview = b.getValue();
            }
        }

        org.endeavourhealth.transform.enterprise.outputModels.Observation model = (org.endeavourhealth.transform.enterprise.outputModels.Observation)csvWriter;
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
                originalTerm,
                isReview);
    }
}


