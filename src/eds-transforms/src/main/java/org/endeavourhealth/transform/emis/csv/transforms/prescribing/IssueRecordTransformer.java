package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_IssueRecord;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class IssueRecordTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Prescribing_IssueRecord parser = new Prescribing_IssueRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, csvProcessor, csvHelper);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createResource(Prescribing_IssueRecord issueParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        MedicationOrder fhirMedication = new MedicationOrder();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        String issueRecordGuid = issueParser.getIssueRecordGuid();
        String patientGuid = issueParser.getPatientGuid();
        String organisationGuid = issueParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirMedication, patientGuid, issueRecordGuid);

        fhirMedication.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (issueParser.getDeleted() || issueParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirMedication, patientGuid);
            return;
        }

        Date effectiveDate = issueParser.getEffectiveDate();
        String effectiveDatePrecision = issueParser.getEffectiveDatePrecision();
        DateTimeType dateTime = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);
        fhirMedication.setDateWrittenElement(dateTime);

        //cache the date against the drug record GUID, so we can pick it up when processing the DrugRecord CSV
        String drugRecordGuid = issueParser.getDrugRecordGuid();
        csvHelper.cacheDrugRecordDate(drugRecordGuid, patientGuid, dateTime);

        String prescriberGuid = issueParser.getClinicianUserInRoleGuid();
        fhirMedication.setPrescriber(csvHelper.createPractitionerReference(prescriberGuid));

        Long codeId = issueParser.getCodeId();
        fhirMedication.setMedication(csvHelper.findMedication(codeId, csvProcessor));

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

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        String problemObservationGuid = issueParser.getProblemObservationGuid();
        if (problemObservationGuid != null) {
            fhirMedication.setReason(csvHelper.createObservationReference(problemObservationGuid, patientGuid));
        }

        Reference authorisationReference = csvHelper.createMedicationStatementReference(drugRecordGuid, patientGuid);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION, authorisationReference));

        csvProcessor.savePatientResource(fhirMedication, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(issueParser.getProblemObservationGuid(),
                patientGuid,
                issueRecordGuid,
                fhirMedication.getResourceType());

    }

}
