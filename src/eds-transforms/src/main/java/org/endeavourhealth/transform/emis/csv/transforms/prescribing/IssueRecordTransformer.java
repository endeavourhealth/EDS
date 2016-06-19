package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_IssueRecord;
import org.endeavourhealth.transform.emis.csv.FhirObjectStore;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class IssueRecordTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        Prescribing_IssueRecord parser = new Prescribing_IssueRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(Prescribing_IssueRecord issueParser, FhirObjectStore objectStore) throws Exception {

        MedicationOrder fhirMedication = new MedicationOrder();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        String issueGuid = issueParser.getIssueRecordGuid();
        fhirMedication.setId(issueGuid);

        String patientGuid = issueParser.getPatientGuid();
        fhirMedication.setPatient(objectStore.createPatientReference(patientGuid));

        boolean store = !issueParser.getDeleted() && !issueParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, fhirMedication, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        Date effectiveDate = issueParser.getEffectiveDate();
        String effectiveDatePrecision = issueParser.getEffectiveDatePrecision();
        DateTimeType dateTime = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);
        fhirMedication.setDateWrittenElement(dateTime);

        String prescriberGuid = issueParser.getClinicianUserInRoleGuid();
        fhirMedication.setPrescriber(objectStore.createPractitionerReference(prescriberGuid, patientGuid));

        Long codeId = issueParser.getCodeId();
        fhirMedication.setMedication(objectStore.createMedicationReference(codeId, patientGuid));

        String dose = issueParser.getDosage();
        MedicationOrder.MedicationOrderDosageInstructionComponent fhirDose = fhirMedication.addDosageInstruction();
        fhirDose.setText(dose);

        Double cost = issueParser.getEstimatedNhsCost();
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_ORDER_ESTIMATED_COST, new DecimalType(cost)));

        Double quantity = issueParser.getQuantity();
        String quantityUnit = issueParser.getQuantityUnit();
        Integer courseDuration = issueParser.getCourseDurationInDays();
        MedicationOrder.MedicationOrderDispenseRequestComponent fhirDispenseRequest = new MedicationOrder.MedicationOrderDispenseRequestComponent();
        fhirDispenseRequest.setQuantity(QuantityHelper.createSimpleQuantity(quantity, quantityUnit));
        fhirDispenseRequest.setExpectedSupplyDuration(QuantityHelper.createDuration(courseDuration, "days"));
        fhirMedication.setDispenseRequest(fhirDispenseRequest);

        String problemObservationGuid = issueParser.getProblemObservationGuid();
        objectStore.linkToProblem(fhirMedication, problemObservationGuid, patientGuid);

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        if (problemObservationGuid != null) {
            fhirMedication.setReason(objectStore.createObservationReference(problemObservationGuid, patientGuid));
        }

        String drugRecordGuid = issueParser.getDrugRecordGuid();
        Reference authorisationReference = objectStore.createMedicationStatementReference(drugRecordGuid, patientGuid);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION, authorisationReference));
    }

}
