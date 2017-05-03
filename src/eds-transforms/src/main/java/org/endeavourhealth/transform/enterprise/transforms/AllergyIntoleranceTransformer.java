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

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AllergyIntoleranceTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AllergyIntoleranceTransformer.class);

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
                          String configName,
                          UUID protocolId) throws Exception {

        AllergyIntolerance fhir = (AllergyIntolerance)resource;

        long id;
        long organisationId;
        long patientId;
        long personId;
        Long encounterId = null;
        Long practitionerId = null;
        Date clinicalEffectiveDate = null;
        Integer datePrecisionId = null;
        Long snomedConceptId = null;
        String originalCode = null;
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
                    encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
                }
            }
        }

        if (fhir.hasRecorder()) {
            Reference practitionerReference = fhir.getRecorder();
            practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            if (practitionerId == null) {
                practitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
            }
        }

        if (fhir.hasOnset()) {
            DateTimeType dt = fhir.getOnsetElement();
            clinicalEffectiveDate = dt.getValue();
            datePrecisionId = convertDatePrecision(dt.getPrecision());

        }

        snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhir.getSubstance());

        //add the raw original code, to assist in data checking
        originalCode = CodeableConceptHelper.findOriginalCode(fhir.getSubstance());

        //add original term too, for easy display of results
        originalTerm = fhir.getSubstance().getText();

        Extension reviewExtension = ExtensionConverter.findExtension(fhir, FhirExtensionUri.IS_REVIEW);
        if (reviewExtension != null) {
            BooleanType b = (BooleanType)reviewExtension.getValue();
            if (b.getValue() != null) {
                isReview = b.getValue();
            }
        }

        org.endeavourhealth.transform.enterprise.outputModels.AllergyIntolerance model = (org.endeavourhealth.transform.enterprise.outputModels.AllergyIntolerance)csvWriter;
        model.writeUpsert(id,
            organisationId,
            patientId,
            personId,
            encounterId,
            practitionerId,
            clinicalEffectiveDate,
            datePrecisionId,
            snomedConceptId,
            originalCode,
            originalTerm,
            isReview);
    }

}

