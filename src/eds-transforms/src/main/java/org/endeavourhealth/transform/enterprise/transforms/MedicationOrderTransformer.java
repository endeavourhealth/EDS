package org.endeavourhealth.transform.enterprise.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.Encounter;
import org.endeavourhealth.core.xml.enterprise.*;
import org.endeavourhealth.core.xml.enterprise.MedicationStatement;
import org.endeavourhealth.core.xml.enterprise.Practitioner;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MedicationOrderTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationOrderTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.MedicationOrder model = new org.endeavourhealth.core.xml.enterprise.MedicationOrder();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            org.hl7.fhir.instance.model.MedicationOrder fhir = (org.hl7.fhir.instance.model.MedicationOrder)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Patient(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasPrescriber()) {
                Reference practitionerReference = fhir.getPrescriber();
                Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(new Encounter(), encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasDateWrittenElement()) {
                DateTimeType dt = fhir.getDateWrittenElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getMedicationCodeableConcept());
            model.setDmdId(snomedConceptId);

            //add term too, for easy display of results
            String originalTerm = fhir.getMedicationCodeableConcept().getText();
            //if we failed to find one, it's because of a change in how the CodeableConcept was generated, so find the term differently
            if (Strings.isNullOrEmpty(originalTerm)) {
                originalTerm = findSnomedConceptText(fhir.getMedicationCodeableConcept());
            }
            model.setOriginalTerm(originalTerm);

            if (fhir.hasDosageInstruction()) {
                if (fhir.getDosageInstruction().size() > 1) {
                    throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
                }

                MedicationOrder.MedicationOrderDosageInstructionComponent doseage = fhir.getDosageInstruction().get(0);
                String dose = doseage.getText();

                //one of the Emis test packs includes the unicode \u0001 character in the dose. This should be handled
                //during the inbound transform, but the data is already in the DB now, so needs handling here
                char[] chars = dose.toCharArray();
                for (int i=0; i<chars.length; i++) {
                    char c = chars[i];
                    if (c == 1) {
                        chars[i] = '?'; //just replace with ?
                    }
                }
                dose = new String(chars);

                model.setDose(dose);
            }

            if (fhir.hasDispenseRequest()) {
                MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequestComponent = fhir.getDispenseRequest();
                Quantity q = dispenseRequestComponent.getQuantity();
                model.setQuantityValue(q.getValue());
                model.setQuantityUnit(q.getUnit());

                Duration duration = dispenseRequestComponent.getExpectedSupplyDuration();
                if (!duration.getUnit().equalsIgnoreCase("days")) {
                    throw new TransformException("Unsupported medication order duration type [" + duration.getUnit() + "] for " + fhir.getId());
                }
                int days = duration.getValue().intValue();
                model.setDurationDays(new Integer(days));
            }

            if (fhir.hasExtension()) {
                for (Extension extension : fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_ORDER_ESTIMATED_COST)) {
                        DecimalType d = (DecimalType)extension.getValue();
                        model.setEstimatedCost(d.getValue());

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION)) {
                        Reference medicationStatementReference = (Reference)extension.getValue();
                        Integer enterprisePractitionerUuid = findEnterpriseId(new MedicationStatement(), medicationStatementReference);

                        //the test pack contains medication orders (i.e. issueRecords) that point to medication statements (i.e. drugRecords)
                        //that don't exist, so log it out and just skip this bad record
                        if (enterprisePractitionerUuid == null) {
                            LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " as it refers to a MedicationStatement that doesn't exist");
                        } else {
                            model.setMedicationStatementId(enterprisePractitionerUuid);
                        }
                    }
                }
            }


        }

        data.getMedicationOrder().add(model);
    }

}

