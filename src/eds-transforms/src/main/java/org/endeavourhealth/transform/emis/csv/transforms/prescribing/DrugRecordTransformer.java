package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_DrugRecord;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.Date;

public class DrugRecordTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Prescribing_DrugRecord parser = new Prescribing_DrugRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, csvProcessor, csvHelper);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(Prescribing_DrugRecord drugRecordParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        MedicationStatement fhirMedication = new MedicationStatement();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        String drugRecordGuid = drugRecordParser.getDrugRecordGuid();
        String patientGuid = drugRecordParser.getPatientGuid();
        String organisationGuid = drugRecordParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirMedication, patientGuid, drugRecordGuid);

        fhirMedication.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (drugRecordParser.getDeleted() || drugRecordParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirMedication, patientGuid);
            return;
        }

        String clinicianGuid = drugRecordParser.getClinicianUserInRoleGuid();
        fhirMedication.setInformationSource(csvHelper.createPractitionerReference(clinicianGuid));

        Date effectiveDate = drugRecordParser.getEffectiveDate();
        String effectiveDatePrecision = drugRecordParser.getEffectiveDatePrecision();
        fhirMedication.setDateAssertedElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        if (drugRecordParser.getIsActive()) {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
        } else {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
        }

        Long codeId = drugRecordParser.getCodeId();
        fhirMedication.setMedication(csvHelper.findMedication(codeId));

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
        csvHelper.linkToProblem(fhirMedication, problemObservationGuid, patientGuid);

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        if (problemObservationGuid != null) {
            fhirMedication.setReasonForUse(csvHelper.createObservationReference(problemObservationGuid, patientGuid));
        }

        Date cancellationDate = drugRecordParser.getCancellationDate();
        if (cancellationDate != null) {
            //TODO - MedicationAuthorisation resource requires cancellation Performer, which isn't available
            //fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION
        }

        //NOTE: first and most recent issue dates are set at the end of the IssueRecordTransformer
        //TODO - first and most recent issue dates on FHIR MedicationStatement

        csvProcessor.savePatientResource(fhirMedication, patientGuid);
    }

}