package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class ObservationTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationTransformer.class);

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

            Observation fhir = (Observation)deserialiseResouce(resource);

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

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
            }

            if (fhir.hasPerformer()) {
                for (Reference reference: fhir.getPerformer()) {
                    ResourceType resourceType = ReferenceHelper.getResourceType(reference);
                    if (resourceType == ResourceType.Practitioner) {
                        practitionerId = findEnterpriseId(data.getPractitioners(), reference);
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

