package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.schema.MedicationAuthorisationType;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.Date;

public class DrugRecordTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        DrugRecord parser = new DrugRecord(folderPath, csvFormat);
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

    private static void createResource(DrugRecord parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        MedicationStatement fhirMedication = new MedicationStatement();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        String drugRecordGuid = parser.getDrugRecordGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirMedication, patientGuid, drugRecordGuid);

        fhirMedication.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirMedication, patientGuid);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirMedication.setInformationSource(csvHelper.createPractitionerReference(clinicianGuid));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirMedication.setDateAssertedElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        if (parser.getIsActive()) {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
        } else {
            fhirMedication.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
        }

        Long codeId = parser.getCodeId();
        fhirMedication.setMedication(csvHelper.findMedication(codeId, csvProcessor));

        String dose = parser.getDosage();
        MedicationStatement.MedicationStatementDosageComponent fhirDose = fhirMedication.addDosage();
        fhirDose.setText(dose);

        Double quantity = parser.getQuantity();
        String quantityUnit = parser.getQuantityUnit();
        Quantity fhirQuantity = new Quantity();
        fhirQuantity.setValue(BigDecimal.valueOf(quantity.doubleValue()));
        fhirQuantity.setUnit(quantityUnit);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY, fhirQuantity));

        Integer issuesAuthorised = parser.getNumberOfIssuesAuthorised();
        if (issuesAuthorised != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED, new PositiveIntType(issuesAuthorised)));
        }

        Integer issuesReceived = parser.getNumberOfIssues();
        if (issuesReceived != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED, new PositiveIntType(issuesReceived)));
        }

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        String problemObservationGuid = parser.getProblemObservationGuid();
        if (problemObservationGuid != null) {
            fhirMedication.setReasonForUse(csvHelper.createObservationReference(problemObservationGuid, patientGuid));
        }

        Date cancellationDate = parser.getCancellationDate();
        if (cancellationDate != null) {
            //the cancellation extension is a compound extension, so we have one extension inside another
            Extension extension = ExtensionConverter.createExtension("performer", new DateType(cancellationDate));
            fhirMedication.addExtension(ExtensionConverter.createCompoundExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION, extension));
        }

        DateTimeType mostRecentDate = csvHelper.getDrugRecordDate(drugRecordGuid, patientGuid);
        if (mostRecentDate != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, mostRecentDate));
        }

        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (!Strings.isNullOrEmpty(enteredByGuid)) {
            Reference reference = csvHelper.createPractitionerReference(enteredByGuid);
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
        }

        Date enteredDateTime = parser.getEnteredDateTime();
        if (enteredDateTime != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
        }

        String authorisationType = parser.getPrescriptionType();
        MedicationAuthorisationType fhirAuthorisationType = MedicationAuthorisationType.fromDescription(authorisationType);
        Coding fhirCoding = CodingHelper.createCoding(fhirAuthorisationType);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE, fhirCoding));

        csvProcessor.savePatientResource(fhirMedication, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(problemObservationGuid,
                                            patientGuid,
                                            drugRecordGuid,
                                            fhirMedication.getResourceType());
    }

}