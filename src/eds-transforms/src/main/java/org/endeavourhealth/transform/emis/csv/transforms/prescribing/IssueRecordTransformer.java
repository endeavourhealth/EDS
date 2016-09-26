package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class IssueRecordTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        IssueRecord parser = new IssueRecord(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(version, parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createResource(String version,
                                       IssueRecord parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        MedicationOrder fhirMedication = new MedicationOrder();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        String issueRecordGuid = parser.getIssueRecordGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirMedication, patientGuid, issueRecordGuid);

        fhirMedication.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirMedication);
            return;
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        DateTimeType dateTime = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);
        fhirMedication.setDateWrittenElement(dateTime);

        //cache the date against the drug record GUID, so we can pick it up when processing the DrugRecord CSV
        String drugRecordGuid = parser.getDrugRecordGuid();
        csvHelper.cacheDrugRecordDate(drugRecordGuid, patientGuid, dateTime);

        //need to handle mis-spelt column name in EMIS test pack
        //String clinicianGuid = parser.getClinicianUserInRoleGuid();
        String clinicianGuid = null;
        if (version.equals(EmisCsvTransformer.VERSION_TEST_PACK)) {
            clinicianGuid = parser.getClinicanUserInRoleGuid();
        } else {
            clinicianGuid = parser.getClinicianUserInRoleGuid();
        }

        fhirMedication.setPrescriber(csvHelper.createPractitionerReference(clinicianGuid));

        Long codeId = parser.getCodeId();
        fhirMedication.setMedication(csvHelper.findMedication(codeId, csvProcessor));

        String dose = parser.getDosage();
        MedicationOrder.MedicationOrderDosageInstructionComponent fhirDose = fhirMedication.addDosageInstruction();
        fhirDose.setText(dose);

        Double cost = parser.getEstimatedNhsCost();
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_ORDER_ESTIMATED_COST, new DecimalType(cost)));

        Double quantity = parser.getQuantity();
        String quantityUnit = parser.getQuantityUnit();
        Integer courseDuration = parser.getCourseDurationInDays();
        MedicationOrder.MedicationOrderDispenseRequestComponent fhirDispenseRequest = new MedicationOrder.MedicationOrderDispenseRequestComponent();
        fhirDispenseRequest.setQuantity(QuantityHelper.createSimpleQuantity(quantity, quantityUnit));
        fhirDispenseRequest.setExpectedSupplyDuration(QuantityHelper.createDuration(courseDuration, "days"));
        fhirMedication.setDispenseRequest(fhirDispenseRequest);

        //if the Medication is linked to a Problem, then use the problem's Observation as the Medication reason
        String problemObservationGuid = parser.getProblemObservationGuid();
        if (!Strings.isNullOrEmpty(problemObservationGuid)) {
            fhirMedication.setReason(csvHelper.createObservationReference(problemObservationGuid, patientGuid));
        }

        Reference authorisationReference = csvHelper.createMedicationStatementReference(drugRecordGuid, patientGuid);
        fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION, authorisationReference));

        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (!Strings.isNullOrEmpty(enteredByGuid)) {
            Reference reference = csvHelper.createPractitionerReference(enteredByGuid);
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
        }

        Date enteredDateTime = parser.getEnteredDateTime();
        if (enteredDateTime != null) {
            fhirMedication.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
        }

        csvProcessor.savePatientResource(patientGuid, fhirMedication);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(problemObservationGuid,
                patientGuid,
                issueRecordGuid,
                fhirMedication.getResourceType());

    }

}
