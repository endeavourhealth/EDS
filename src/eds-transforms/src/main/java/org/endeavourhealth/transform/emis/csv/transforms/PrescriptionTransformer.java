package org.endeavourhealth.transform.emis.csv.transforms;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.CsvDrugStatus;
import org.endeavourhealth.transform.emis.csv.schema.CsvPrescription;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class PrescriptionTransformer {

    public static void transform(CSVParser prescriptionCsv, Map<String, List<Resource>> fhirMap, int prescriptionCount) throws Exception {

        int row = 0;
        for (CSVRecord csvRecord : prescriptionCsv) {
            transform(csvRecord, fhirMap);
            row ++;
        }

        if (row != prescriptionCount) {
            throw new TransformException("Mismatch in number of patient rows. Expected " + prescriptionCount + " got " + row);
        }
    }

    public static void transform(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        MedicationStatement fhirMedication = new MedicationStatement();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        String id = csvRecord.get(CsvPrescription.ID.getValue());
        fhirMedication.setId(id);

        String careRecordId = csvRecord.get(CsvPrescription.CARERECORDID.getValue());

        //add the resource to the map
        List<Resource> fhirResources = fhirMap.get(careRecordId);
        if (fhirResources == null){
            throw new TransformException("No patient resource for care record ID " + careRecordId);
        }
        fhirResources.add(fhirMedication);

        fhirMedication.setPatient(Fhir.createPatientReference(careRecordId));
        fhirMedication.setInformationSource(Fhir.createOrganisationReference(fhirResources));

        DateFormat df = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT); //using old date API as FHIR does
        String issueDateStr = csvRecord.get(CsvPrescription.ISSUEDATE.getValue());
        Date issueDate = df.parse(issueDateStr);
        fhirMedication.setDateAsserted(issueDate);

        String dose = csvRecord.get(CsvPrescription.DIRECTIONS.getValue());
        MedicationStatement.MedicationStatementDosageComponent fhirDosage = fhirMedication.addDosage();
        fhirDosage.setText(dose);

        String quantity = csvRecord.get(CsvPrescription.SUPPLY.getValue());
        fhirMedication.addExtension(Fhir.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY, new StringType(quantity)));

        String lastIssueDateStr = csvRecord.get(CsvPrescription.LAST_ISSUE_DATE.getValue());
        if (!Strings.isNullOrEmpty(lastIssueDateStr)) {
            Date lastIssueDate = df.parse(lastIssueDateStr);
            fhirMedication.addExtension(Fhir.createExtension(FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, new DateType(lastIssueDate)));
        }

        String status = csvRecord.get(CsvPrescription.STATUS.getValue());
        fhirMedication.setStatus(transformStatus(status));

        String issueType = csvRecord.get(CsvPrescription.ISSUETYPE.getValue());
        //TODO - need extension of MedicationOrder to store issue type

        fhirMedication.setMedication(convertMedication(csvRecord));
    }


    private static Type convertMedication(CSVRecord csvRecord) throws TransformException {

        String readCode = csvRecord.get(CsvPrescription.READCODE.getValue());
        String drugName = csvRecord.get(CsvPrescription.DRUG.getValue());
        //String emisCode = csvRecord.get(CsvPrescription.EMISCODE.getValue());

        if (!Strings.isNullOrEmpty(readCode)) {
            return Fhir.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, drugName, readCode);
        } else {
            return Fhir.createCodeableConcept(drugName);
        }
    }

    private static MedicationStatement.MedicationStatementStatus transformStatus(String drugStatus)
    {
        CsvDrugStatus drugStatusObj = CsvDrugStatus.fromValue(drugStatus);
        switch (drugStatusObj)
        {
            case A:
                return MedicationStatement.MedicationStatementStatus.ACTIVE;
            case N:
                return MedicationStatement.MedicationStatementStatus.ENTEREDINERROR;
            case C:
            default:
                return MedicationStatement.MedicationStatementStatus.COMPLETED;
        }
    }
}
