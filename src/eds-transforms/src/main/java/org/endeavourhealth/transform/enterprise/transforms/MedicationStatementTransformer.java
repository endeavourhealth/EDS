package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.schema.MedicationAuthorisationType;
import org.hl7.fhir.instance.model.*;

import java.util.Map;

public class MedicationStatementTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.MedicationStatement model = new org.endeavourhealth.core.xml.enterprise.MedicationStatement();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            MedicationStatement fhir = (MedicationStatement)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasInformationSource()) {
                Reference practitionerReference = fhir.getInformationSource();
                Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasDateAssertedElement()) {
                DateTimeType dt = fhir.getDateAssertedElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getMedicationCodeableConcept());
            model.setDmdId(snomedConceptId);

            if (fhir.hasStatus()) {
                MedicationStatement.MedicationStatementStatus fhirStatus = fhir.getStatus();
                model.setIsActive(fhirStatus == MedicationStatement.MedicationStatementStatus.ACTIVE);
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
                        model.setMedicationStatementAuthorisationTypeId(type.ordinal());
                    }
                }
            }
        }

        data.getMedicationStatement().add(model);
    }



}

