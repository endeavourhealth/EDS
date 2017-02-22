package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class DiagnosticOrderTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderTransformer.class);

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

            DiagnosticOrder fhir = (DiagnosticOrder)deserialiseResouce(resource);

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

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
            }

            if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            }

            if (fhir.hasEvent()) {
                DiagnosticOrder.DiagnosticOrderEventComponent event = fhir.getEvent().get(0);
                if (event.hasDateTimeElement()) {
                    DateTimeType dt = event.getDateTimeElement();
                    clinicalEffectiveDate = dt.getValue();
                    datePrecisionId = convertDatePrecision(dt.getPrecision());
                }
            }

            if (fhir.getItem().size() > 1) {
                throw new TransformException("DiagnosticOrder with more than one item not supported");
            }
            DiagnosticOrder.DiagnosticOrderItemComponent item = fhir.getItem().get(0);
            snomedConceptId = CodeableConceptHelper.findSnomedConceptId(item.getCode());

            //add the raw original code, to assist in data checking
            originalCode = CodeableConceptHelper.findOriginalCode(item.getCode());

            //add original term too, for easy display of results
            originalTerm = item.getCode().getText();

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

        //org.endeavourhealth.core.xml.enterprise.DiagnosticOrder model = new org.endeavourhealth.core.xml.enterprise.DiagnosticOrder();
        org.endeavourhealth.core.xml.enterprise.Observation model = new org.endeavourhealth.core.xml.enterprise.Observation();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            DiagnosticOrder fhir = (DiagnosticOrder)deserialiseResouce(resource);

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

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(new Encounter(), encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasEvent()) {
                DiagnosticOrder.DiagnosticOrderEventComponent event = fhir.getEvent().get(0);
                if (event.hasDateTimeElement()) {
                    DateTimeType dt = event.getDateTimeElement();
                    model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                    model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
                }
            }

            if (fhir.getItem().size() > 1) {
                throw new TransformException("DiagnosticOrder with more than one item not supported");
            }
            DiagnosticOrder.DiagnosticOrderItemComponent item = fhir.getItem().get(0);
            Long snomedConceptId = findSnomedConceptId(item.getCode());
            model.setSnomedConceptId(snomedConceptId);

            //add the raw original code, to assist in data checking
            String originalCode = findOriginalCode(item.getCode());
            model.setOriginalCode(originalCode);

            //add original term too, for easy display of results
            String originalTerm = item.getCode().getText();
            model.setOriginalTerm(originalTerm);
        }

        data.getObservation().add(model);
    }
*/

}

