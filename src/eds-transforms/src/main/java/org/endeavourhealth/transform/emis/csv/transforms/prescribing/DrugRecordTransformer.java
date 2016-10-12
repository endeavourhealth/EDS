package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.schema.MedicationAuthorisationType;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class DrugRecordTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        DrugRecord parser = (DrugRecord)parsers.get(DrugRecord.class);

        while (parser.nextRecord()) {

            try {
                createResource(version, parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createResource(String version,
                                       DrugRecord parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        MedicationStatement fhirMedicationStatement = new MedicationStatement();
        fhirMedicationStatement.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        String drugRecordGuid = parser.getDrugRecordGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirMedicationStatement, patientGuid, drugRecordGuid);

        fhirMedicationStatement.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(parser.getCurrentState(), patientGuid, fhirMedicationStatement);
            return;
        }

        //need to handle mis-spelt column name in EMIS test pack
        //String clinicianGuid = parser.getClinicianUserInRoleGuid();
        String clinicianGuid = null;
        if (version.equals(EmisCsvTransformer.VERSION_TEST_PACK)) {
            clinicianGuid = parser.getClinicanUserInRoleGuid();
        } else {
            clinicianGuid = parser.getClinicianUserInRoleGuid();
        }

        fhirMedicationStatement.setInformationSource(csvHelper.createPractitionerReference(clinicianGuid));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirMedicationStatement.setDateAssertedElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        if (parser.getIsActive()) {
            fhirMedicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);
        } else {
            fhirMedicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);
        }

        Long codeId = parser.getCodeId();
        fhirMedicationStatement.setMedication(csvHelper.findMedication(codeId, csvProcessor));

        String dose = parser.getDosage();
        MedicationStatement.MedicationStatementDosageComponent fhirDose = fhirMedicationStatement.addDosage();
        fhirDose.setText(dose);

        Double quantity = parser.getQuantity();
        String quantityUnit = parser.getQuantityUnit();
        Quantity fhirQuantity = new Quantity();
        fhirQuantity.setValue(BigDecimal.valueOf(quantity.doubleValue()));
        fhirQuantity.setUnit(quantityUnit);
        fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY, fhirQuantity));

        Integer issuesAuthorised = parser.getNumberOfIssuesAuthorised();
        if (issuesAuthorised != null) {
            fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED, new PositiveIntType(issuesAuthorised)));
        }

        Integer issuesReceived = parser.getNumberOfIssues();
        if (issuesReceived != null) {
            fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED, new PositiveIntType(issuesReceived)));
        }

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        String problemObservationGuid = parser.getProblemObservationGuid();
        if (!Strings.isNullOrEmpty(problemObservationGuid)) {
            fhirMedicationStatement.setReasonForUse(csvHelper.createObservationReference(problemObservationGuid, patientGuid));
        }

        Date cancellationDate = parser.getCancellationDate();
        if (cancellationDate != null) {
            //the cancellation extension is a compound extension, so we have one extension inside another
            Extension extension = ExtensionConverter.createExtension("performer", new DateType(cancellationDate));
            fhirMedicationStatement.addExtension(ExtensionConverter.createCompoundExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION, extension));
        }

        DateTimeType mostRecentDate = csvHelper.getDrugRecordDate(drugRecordGuid, patientGuid);
        if (mostRecentDate != null) {
            fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, mostRecentDate));
        }

        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (!Strings.isNullOrEmpty(enteredByGuid)) {
            Reference reference = csvHelper.createPractitionerReference(enteredByGuid);
            fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
        }

        Date enteredDateTime = parser.getEnteredDateTime();
        if (enteredDateTime != null) {
            fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
        }

        String authorisationType = parser.getPrescriptionType();
        MedicationAuthorisationType fhirAuthorisationType = MedicationAuthorisationType.fromDescription(authorisationType);
        Coding fhirCoding = CodingHelper.createCoding(fhirAuthorisationType);
        fhirMedicationStatement.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE, fhirCoding));

        csvProcessor.savePatientResource(parser.getCurrentState(), patientGuid, fhirMedicationStatement);

/*        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(problemObservationGuid,
                                            patientGuid,
                                            drugRecordGuid,
                                            fhirMedicationStatement.getResourceType());*/
    }

}