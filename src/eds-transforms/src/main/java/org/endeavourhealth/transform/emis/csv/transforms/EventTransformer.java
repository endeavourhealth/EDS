package org.endeavourhealth.transform.emis.csv.transforms;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.CsvEvent;
import org.endeavourhealth.transform.emis.csv.schema.CsvPrescription;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.ConditionClinicalEnumFactory;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class EventTransformer {

    public static void transform(CSVParser eventCsv, Map<String, List<Resource>> fhirMap, int eventCount) throws Exception {

        int row = 0;
        for (CSVRecord csvRecord : eventCsv) {
            transform(csvRecord, fhirMap);
            row ++;
        }

        if (row != eventCount) {
            throw new TransformException("Mismatch in number of patient rows. Expected " + eventCount + " got " + row);
        }
    }

    public static void transform(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        String readCode = csvRecord.get(CsvEvent.READCODE.getValue());

        if (TerminologyService.isRead2Procedure(readCode)) {
            transformProcedure(csvRecord, fhirMap);
        } else if (TerminologyService.isRead2Observation(readCode)) {
            transformObservation(csvRecord, fhirMap);
        } else if (TerminologyService.isRead2FamilyHistory(readCode)) {
            transformFamilyHistory(csvRecord, fhirMap);
        } else if (TerminologyService.isRead2Condition(readCode)) {
            transformCondition(csvRecord, fhirMap);
        } else if (TerminologyService.isRead2Allergy(readCode)) {
            transformAllergyIntolerance(csvRecord, fhirMap);
        } else {
            throw new TransformException("Unknown read code type " + readCode);
        }
    }

    private static void transformProcedure(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_PROCEDURE));

        String id = csvRecord.get(CsvEvent.ID.getValue());
        fhirProcedure.setId(id);

        String careRecordId = csvRecord.get(CsvEvent.CARERECORDID.getValue());
        fhirProcedure.setSubject(Fhir.createPatientReference(careRecordId));

        //add the resource to the map
        addToMap(fhirProcedure, careRecordId, fhirMap);

        String dateStr = csvRecord.get(CsvEvent.ODATE.getValue());
        Date date = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT).parse(dateStr);
        fhirProcedure.setPerformed(new DateType(date));

        String code = csvRecord.get(CsvEvent.READCODE.getValue());
        String term = csvRecord.get(CsvEvent.READTERM.getValue());
        fhirProcedure.setCode(Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_READ2, term, code));

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        //ensure there are no numeric values that will be ignored
        ensureNoValues(csvRecord);
    }

    private static void transformObservation(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {
        Observation fhirObservation = new Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_OBSERVATION));

        String id = csvRecord.get(CsvEvent.ID.getValue());
        fhirObservation.setId(id);

        String careRecordId = csvRecord.get(CsvEvent.CARERECORDID.getValue());
        fhirObservation.setSubject(Fhir.createPatientReference(careRecordId));

        //add the resource to the map
        addToMap(fhirObservation, careRecordId, fhirMap);

        String dateStr = csvRecord.get(CsvEvent.ODATE.getValue());
        Date date = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT).parse(dateStr);
        fhirObservation.setEffective(new DateType(date));

        String code = csvRecord.get(CsvEvent.READCODE.getValue());
        String term = csvRecord.get(CsvEvent.READTERM.getValue());
        fhirObservation.setCode(Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_READ2, term, code));

        fhirObservation.setStatus(Observation.ObservationStatus.FINAL);

        String valueOne = csvRecord.get(CsvEvent.NUMRESULT.getValue());
        String valueTwo = csvRecord.get(CsvEvent.NUMRESULT2.getValue());

        if (!Strings.isNullOrEmpty(valueOne) && Strings.isNullOrEmpty(valueTwo)) {
            //just value one
            Quantity q = createQuantity(valueOne);
            fhirObservation.setValue(q);

        } else if (Strings.isNullOrEmpty(valueOne) && !Strings.isNullOrEmpty(valueTwo)) {
            //just value two
            Quantity q = createQuantity(valueTwo);
            fhirObservation.setValue(q);

        } else if (!Strings.isNullOrEmpty(valueOne) && !Strings.isNullOrEmpty(valueTwo)) {
            //both values
            Quantity q = createQuantity(valueOne);
            Quantity q2 = createQuantity(valueTwo);

            Observation.ObservationComponentComponent fhirComponent = fhirObservation.addComponent();
            fhirComponent.setCode(Fhir.createCodeableConcept("Systolic"));
            fhirComponent.setValue(q);

            fhirComponent = fhirObservation.addComponent();
            fhirComponent.setCode(Fhir.createCodeableConcept("Diastolic"));
            fhirComponent.setValue(q2);
        }
    }

    private static Quantity createQuantity(String valueStr) {
        BigDecimal d = new BigDecimal(valueStr);
        Quantity fhirQuantity = new Quantity();
        fhirQuantity.setValue(d);
        return fhirQuantity;
    }

    private static void ensureNoValues(CSVRecord csvRecord) throws Exception {
        String valueOne = csvRecord.get(CsvEvent.NUMRESULT.getValue());
        if (!Strings.isNullOrEmpty(valueOne)) {
            throw new TransformException("" + CsvEvent.NUMRESULT + " present in non-observation row");
        }
        String valueTwo = csvRecord.get(CsvEvent.NUMRESULT2.getValue());
        if (!Strings.isNullOrEmpty(valueTwo)) {
            throw new TransformException("" + CsvEvent.NUMRESULT + " present in non-observation row");
        }
    }

    private static void transformFamilyHistory(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String id = csvRecord.get(CsvEvent.ID.getValue());
        fhirFamilyHistory.setId(id);

        String careRecordId = csvRecord.get(CsvEvent.CARERECORDID.getValue());
        fhirFamilyHistory.setPatient(Fhir.createPatientReference(careRecordId));

        //add the resource to the map
        addToMap(fhirFamilyHistory, careRecordId, fhirMap);

        String dateStr = csvRecord.get(CsvEvent.ODATE.getValue());
        Date date = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT).parse(dateStr);
        fhirFamilyHistory.setDate(date);

        fhirFamilyHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);

        fhirFamilyHistory.setRelationship(Fhir.createCodeableConcept(FhirUris.VALUE_SET_FAMILY_MEMBER, FhirUris.VALUE_SET_FAMILY_MEMBER_TERM, FhirUris.VALUE_SET_FAMILY_MEMBER_CODE));

        String code = csvRecord.get(CsvEvent.READCODE.getValue());
        String term = csvRecord.get(CsvEvent.READTERM.getValue());
        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirComponent = fhirFamilyHistory.addCondition();
        fhirComponent.setCode(Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_READ2, term, code));

        //ensure there are no numeric values that will be ignored
        ensureNoValues(csvRecord);
    }

    private static void transformCondition(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_CONDITION));

        String id = csvRecord.get(CsvEvent.ID.getValue());
        fhirCondition.setId(id);

        String careRecordId = csvRecord.get(CsvEvent.CARERECORDID.getValue());
        fhirCondition.setPatient(Fhir.createPatientReference(careRecordId));

        //add the resource to the map
        addToMap(fhirCondition, careRecordId, fhirMap);

        String dateStr = csvRecord.get(CsvEvent.ODATE.getValue());
        Date date = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT).parse(dateStr);
        fhirCondition.setDateRecorded(date);

        String code = csvRecord.get(CsvEvent.READCODE.getValue());
        String term = csvRecord.get(CsvEvent.READTERM.getValue());
        fhirCondition.setCode(Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_READ2, term, code));

        //TODO - set clinicalStatus on Condition resource
        //fhirCondition.setClinicalStatusElement(ClinicalStatus))

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        //ensure there are no numeric values that will be ignored
        ensureNoValues(csvRecord);
    }

    private static void transformAllergyIntolerance(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ALLERGY_INTOLERANCE));

        String id = csvRecord.get(CsvEvent.ID.getValue());
        fhirAllergy.setId(id);

        String careRecordId = csvRecord.get(CsvEvent.CARERECORDID.getValue());
        fhirAllergy.setPatient(Fhir.createPatientReference(careRecordId));

        //add the resource to the map
        addToMap(fhirAllergy, careRecordId, fhirMap);

        String dateStr = csvRecord.get(CsvEvent.ODATE.getValue());
        Date date = new SimpleDateFormat(EmisCsvTransformer.DATE_FORMAT).parse(dateStr);
        fhirAllergy.setRecordedDate(date);

        //TODO - must set the substance on AllergyIntolerance resource

        //ensure there are no numeric values that will be ignored
        ensureNoValues(csvRecord);
    }

    private static void addToMap(Resource resource, String careRecordId, Map<String, List<Resource>> fhirMap) throws TransformException {

        List<Resource> fhirResources = fhirMap.get(careRecordId);
        if (fhirResources == null){
            throw new TransformException("No patient resource for care record ID " + careRecordId);
        }

        fhirResources.add(resource);
    }

}
