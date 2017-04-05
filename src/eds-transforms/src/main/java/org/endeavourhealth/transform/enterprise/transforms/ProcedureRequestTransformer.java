package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class ProcedureRequestTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureRequestTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName) throws Exception {

        Long enterpriseId = mapId(resource, csvWriter, true);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            csvWriter.writeDelete(enterpriseId.longValue());

        } else {
            Resource fhir = deserialiseResouce(resource);
            transform(enterpriseId, fhir, data, csvWriter, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
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
                          String configName) throws Exception {

        ProcedureRequest fhir = (ProcedureRequest)resource;

        long id;
        long organisationId;
        long patientId;
        long personId;
        Long encounterId = null;
        Long practitionerId = null;
        Date clinicalEffectiveDate = null;
        Integer datePrecisionId = null;
        Long snomedConceptId = null;
        int procedureRequestStatusId;
        String originalCode = null;
        String originalTerm = null;

        id = enterpriseId.longValue();
        organisationId = enterpriseOrganisationId.longValue();
        patientId = enterprisePatientId.longValue();
        personId = enterprisePersonId.longValue();

        if (fhir.hasEncounter()) {
            Reference encounterReference = fhir.getEncounter();
            encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
        }

        if (fhir.hasOrderer()) {
            Reference practitionerReference = fhir.getOrderer();
            practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            if (practitionerId == null) {
                practitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
            }
        }

        if (fhir.hasScheduledDateTimeType()) {
            DateTimeType dt = fhir.getScheduledDateTimeType();
            clinicalEffectiveDate = dt.getValue();
            datePrecisionId = convertDatePrecision(dt.getPrecision());
        }

        snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhir.getCode());

        procedureRequestStatusId = fhir.getStatus().ordinal();

        //add the raw original code, to assist in data checking
        originalCode = CodeableConceptHelper.findOriginalCode(fhir.getCode());

        //add original term too, for easy display of results
        originalTerm = fhir.getCode().getText();

        org.endeavourhealth.transform.enterprise.outputModels.ProcedureRequest model = (org.endeavourhealth.transform.enterprise.outputModels.ProcedureRequest)csvWriter;
        model.writeUpsert(id,
            organisationId,
            patientId,
            personId,
            encounterId,
            practitionerId,
            clinicalEffectiveDate,
            datePrecisionId,
            snomedConceptId,
            procedureRequestStatusId,
            originalCode,
            originalTerm);
    }
}

