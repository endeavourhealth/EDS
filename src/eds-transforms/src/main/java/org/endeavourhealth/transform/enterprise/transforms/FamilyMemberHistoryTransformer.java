package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FamilyMemberHistoryTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(FamilyMemberHistoryTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory model = new org.endeavourhealth.core.xml.enterprise.FamilyMemberHistory();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            FamilyMemberHistory fhir = (FamilyMemberHistory)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);

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
                        Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                        model.setEncounterId(enterpriseEncounterUuid);

                    } else if (extension.getUrl().equals(FhirExtensionUri.FAMILY_MEMBER_HISTORY_REPORTED_BY)) {
                        Reference practitionerReference = (Reference)extension.getValue();
                        Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            if (fhir.getCondition().size() > 1) {
                throw new TransformException("FamilyMemberHistory with more than one item not supported");
            }
            FamilyMemberHistory.FamilyMemberHistoryConditionComponent condition = fhir.getCondition().get(0);
            Long snomedConceptId = findSnomedConceptId(condition.getCode());
            model.setSnomedConceptId(snomedConceptId);

            //add the raw original code, to assist in data checking
            String originalCode = findOriginalCode(condition.getCode());
            model.setOriginalCode(originalCode);
        }

        data.getFamilyMemberHistory().add(model);
    }


}

