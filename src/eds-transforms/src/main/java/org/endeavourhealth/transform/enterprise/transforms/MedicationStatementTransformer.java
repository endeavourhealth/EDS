package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.enterprise.schema.MedicationStatementAuthorisationType;
import org.endeavourhealth.transform.enterprise.schema.MedicationStatementStatus;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.schema.MedicationAuthorisationType;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class MedicationStatementTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.MedicationStatement model = new org.endeavourhealth.transform.enterprise.schema.MedicationStatement();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            MedicationStatement fhir = (MedicationStatement)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasInformationSource()) {
                Reference practitionerReference = fhir.getInformationSource();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            DateTimeType dt = fhir.getDateAssertedElement();
            model.setDate(convertDate(dt.getValue()));
            model.setDatePrecision(convertDatePrecision(dt.getPrecision()));

            Long snomedConceptId = findSnomedConceptId(fhir.getMedicationCodeableConcept());
            model.setDmdId(snomedConceptId);

            if (fhir.hasStatus()) {
                MedicationStatement.MedicationStatementStatus status = fhir.getStatus();
                model.setStatus(convertMedicationStatus(status));
            }

            if (fhir.hasDosage()) {
                if (fhir.getDosage().size() > 1) {
                    throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
                }

                MedicationStatement.MedicationStatementDosageComponent doseage = fhir.getDosage().get(0);
                model.setDose(doseage.getText());
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION)) {
                        DateType d = (DateType)extension.getValue();
                        model.setCancellationDate(convertDate(d.getValue()));

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)) {
                        Quantity q = (Quantity)extension.getValue();
                        model.setQuantityValue(q.getValue());
                        model.setQuantityUnit(q.getUnit());

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE)) {
                        Coding c = (Coding)extension.getValue();
                        MedicationAuthorisationType type = MedicationAuthorisationType.fromCode(c.getCode());
                        model.setAuthorisationType(convertAuthorisationType(type));
                    }
                }
            }
        }

        data.getMedicationStatement().add(model);
    }

    private static MedicationStatementAuthorisationType convertAuthorisationType(MedicationAuthorisationType code) throws Exception {
        switch (code) {
            case ACUTE:
                return MedicationStatementAuthorisationType.ACUTE;
            case REPEAT:
                return MedicationStatementAuthorisationType.REPEAT;
            case REPEAT_DISPENSING:
                return MedicationStatementAuthorisationType.REPEAT_DISPENSING;
            case AUTOMATIC:
                return MedicationStatementAuthorisationType.AUTOMATIC;
            default:
                throw new TransformException("Unsupported medication statement authorisation type " + code);
        }
    }

    private static MedicationStatementStatus convertMedicationStatus(MedicationStatement.MedicationStatementStatus status) throws Exception {
        switch (status) {
            case ACTIVE:
                return MedicationStatementStatus.ACTIVE;
            case COMPLETED:
                return MedicationStatementStatus.COMPLETED;
            case ENTEREDINERROR:
                return MedicationStatementStatus.ENTERED_IN_ERROR;
            case INTENDED:
                return MedicationStatementStatus.INTENDED;
            default:
                throw new TransformException("Unexpected medication status " + status);
        }
    }


}

