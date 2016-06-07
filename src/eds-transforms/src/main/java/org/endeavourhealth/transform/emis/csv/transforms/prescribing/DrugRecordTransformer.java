package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_DrugRecord;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DrugRecordTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        Prescribing_DrugRecord parser = new Prescribing_DrugRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(Prescribing_DrugRecord drugRecordParser, FhirObjectStore objectStore) throws Exception {

        //not processing deltas, so just skip any deleted data
        if (drugRecordParser.getDeleted()) {
            return;
        }

        //EDS shouldn't be storing confidential data
        if (drugRecordParser.getIsConfidential()) {
            return;
        }

        MedicationStatement fhirMedication = new MedicationStatement();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        String drugRecordGuid = drugRecordParser.getDrugRecordGuid();
        fhirMedication.setId(drugRecordGuid);

        String patientGuid = drugRecordParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirMedication);

        fhirMedication.setPatient(objectStore.createPatientReference(patientGuid));

        String clinicianGuid = drugRecordParser.getClinicianUserInRoleGuid();
        fhirMedication.setInformationSource(objectStore.createPractitionerReference(clinicianGuid, patientGuid));

        Date effectiveDate = drugRecordParser.getEffectiveDate();
        String effectiveDatePrecision = drugRecordParser.getEffectiveDatePrecision();
        fhirMedication.setDateAssertedElement(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        if (drugRecordParser.getIsActive()) {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
        } else {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
        }

        Long codeId = drugRecordParser.getCodeId();
        fhirMedication.setMedication(objectStore.createMedicationReference(codeId, patientGuid));

        String dose = drugRecordParser.getDosage();
        MedicationStatement.MedicationStatementDosageComponent fhirDose = fhirMedication.addDosage();
        fhirDose.setText(dose);

        Double quantity = drugRecordParser.getQuantity();
        String quantityUnit = drugRecordParser.getQuantityUnit();
        Quantity fhirQuantity = new Quantity();
        fhirQuantity.setValue(BigDecimal.valueOf(quantity.doubleValue()));
        fhirQuantity.setUnit(quantityUnit);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY, fhirQuantity));

        Integer issuesAuthorised = drugRecordParser.getNumberOfIssuesAuthorised();
        if (issuesAuthorised != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED, new PositiveIntType(issuesAuthorised)));
        }

        Integer issuesReceived = drugRecordParser.getNumberOfIssues();
        if (issuesReceived != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED, new PositiveIntType(issuesReceived)));
        }

        String problemObservationGuid = drugRecordParser.getProblemObservationGuid();
        objectStore.linkToProblem(fhirMedication, problemObservationGuid, patientGuid);

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        if (problemObservationGuid != null) {
            fhirMedication.setReasonForUse(objectStore.createObservationReference(problemObservationGuid, patientGuid));
        }

        Date cancellationDate = drugRecordParser.getCancellationDate();
        if (cancellationDate != null) {
            //TODO - MedicationAuthorisation resource requires cancellation Performer, which isn't available
            //fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION
        }

        //NOTE: first and most recent issue dates are set at the end of the IssueRecordTransformer
    }

}