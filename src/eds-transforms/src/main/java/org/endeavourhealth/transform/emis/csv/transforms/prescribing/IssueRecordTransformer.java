package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_IssueRecord;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class IssueRecordTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        //only after parsing all the issue records can we set the first and last dates on the MedicationStatements
        Map<String, DateTimeType> hmFirstDates = new HashMap<>();
        Map<String, DateTimeType> hmLastDates = new HashMap<>();

        Prescribing_IssueRecord parser = new Prescribing_IssueRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, objectStore, hmFirstDates, hmLastDates);
            }
        } finally {
            parser.close();
        }

        //having parsed all the medicationOrders and linked them to the medicationStatements, now
        //hash and sort them to set the first and last dates on the medicationStatements
        Map<String, List<Resource>> fhirResources = objectStore.getFhirPatientResources();
        Iterator<String> it = fhirResources.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            List<Resource> patientResources = fhirResources.get(key);
            assignMinAndMaxDates(patientResources, hmFirstDates, hmLastDates);
        }
    }

    private static void createResource(Prescribing_IssueRecord issueParser, FhirObjectStore objectStore,
                                       Map<String, DateTimeType> hmFirstDates, Map<String, DateTimeType> hmLastDates) throws Exception {

        if (issueParser.getIsConfidential()) {
            return;
        }

        if (issueParser.getDeleted()) {
            return;
        }

        MedicationOrder fhirMedication = new MedicationOrder();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        String patientGuid = issueParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirMedication);

        fhirMedication.setPatient(objectStore.createPatientReference(patientGuid));

        String issueGuid = issueParser.getIssueRecordGuid();
        fhirMedication.setId(issueGuid);

        Date effectiveDate = issueParser.getEffectiveDate();
        String effectiveDatePrecision = issueParser.getEffectiveDatePrecision();
        DateTimeType dateTime = FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision);
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

        DateTimeType firstDateTime = hmFirstDates.get(drugRecordGuid);
        if (firstDateTime != null) {
            if (dateTime.before(firstDateTime)) {
                hmFirstDates.put(drugRecordGuid, dateTime);
            }
        } else {
            hmFirstDates.put(drugRecordGuid, dateTime);
        }

        DateTimeType lastDateTime = hmLastDates.get(drugRecordGuid);
        if (firstDateTime != null) {
            if (dateTime.after(lastDateTime)) {
                hmLastDates.put(drugRecordGuid, dateTime);
            }
        } else {
            hmLastDates.put(drugRecordGuid, dateTime);
        }
    }

    /**
     * takes a list of Resources for a patient and calculates the first and last date of issue for MedicationStatements
     */
    private static void assignMinAndMaxDates(List<Resource> resources,
                                             Map<String, DateTimeType> hmFirstDates,
                                             Map<String, DateTimeType> hmLastDates) {

        List<MedicationStatement> medicationStatements = resources
                .stream()
                .filter(e -> e.getResourceType() == ResourceType.MedicationStatement)
                .map(e -> (MedicationStatement)e)
                .collect(Collectors.toList());

        for (MedicationStatement medicationStatement: medicationStatements) {
            String drugRecordGuid = medicationStatement.getId();
            DateTimeType firstDate = hmFirstDates.get(drugRecordGuid);
            DateTimeType lastDate = hmLastDates.get(drugRecordGuid);

            if (firstDate != null) {
                medicationStatement.addExtension(
                        ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_FIRST_ISSUE_DATE, firstDate));
            }
            if (lastDate != null) {
                medicationStatement.addExtension(
                        ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, lastDate));
            }
        }

    }
}
