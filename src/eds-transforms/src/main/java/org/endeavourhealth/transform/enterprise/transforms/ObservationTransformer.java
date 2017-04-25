package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
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

public class ObservationTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName,
                          UUID protocolId) throws Exception {

        Long enterpriseId = mapId(resource, csvWriter, true);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            csvWriter.writeDelete(enterpriseId.longValue());

        } else {
            Resource fhir = deserialiseResouce(resource);
            transform(enterpriseId, fhir, data, csvWriter, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
        }
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

        Observation fhir = (Observation)resource;

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

        if (fhir.hasEncounter()) {
            Reference encounterReference = fhir.getEncounter();
            encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
        }

        if (fhir.hasPerformer()) {
            for (Reference reference: fhir.getPerformer()) {
                ResourceType resourceType = ReferenceHelper.getResourceType(reference);
                if (resourceType == ResourceType.Practitioner) {
                    practitionerId = findEnterpriseId(data.getPractitioners(), reference);
                    if (practitionerId == null) {
                        practitionerId = transformOnDemand(reference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
                    }
                }
            }
        }

        if (fhir.hasEffectiveDateTimeType()) {
            DateTimeType dt = fhir.getEffectiveDateTimeType();
            clinicalEffectiveDate = dt.getValue();
            datePrecisionId = convertDatePrecision(dt.getPrecision());
        }

        snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhir.getCode());

        if (fhir.hasValue()) {
            Quantity quantity = fhir.getValueQuantity();
            value = quantity.getValue();
            units = quantity.getUnit();
        }

        //add the raw original code, to assist in data checking
        originalCode = CodeableConceptHelper.findOriginalCode(fhir.getCode());

        //add original term too, for easy display of results
        originalTerm = fhir.getCode().getText();

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



