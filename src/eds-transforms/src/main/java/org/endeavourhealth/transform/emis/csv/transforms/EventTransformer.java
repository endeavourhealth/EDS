package org.endeavourhealth.transform.emis.csv.transforms;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.CsvEvent;
import org.endeavourhealth.transform.emis.csv.schema.CsvPrescription;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

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

        //TODO - need to be able to work out if a code is problem
        if (isProblem(readCode)) {
            transformProblem(csvRecord, fhirMap);
        }

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

    private static boolean isProblem(String readCode) {
        //TODO - determine if Read code is a problem, if possible?
        return false;
    }



    private static void transformProcedure(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

    }

    /**
     ID(0),
     CARERECORDID(1),
     PracticeCode(2),
     ODATE(3),
     READCODE(4),
     READTERM(5),
     NUMRESULT(6),
     NUMRESULT2(7);
     */

    private static void transformProblem(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

    }

    /**
     ID(0),
     CARERECORDID(1),
     PracticeCode(2),
     ODATE(3),
     READCODE(4),
     READTERM(5),
     NUMRESULT(6),
     NUMRESULT2(7);
     */

    private static void transformObservation(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

    }

    /**
     ID(0),
     CARERECORDID(1),
     PracticeCode(2),
     ODATE(3),
     READCODE(4),
     READTERM(5),
     NUMRESULT(6),
     NUMRESULT2(7);
     */

    private static void transformFamilyHistory(CSVRecord csvRecord, Map<String, List<Resource>> fhirMap) throws Exception {

    }

    /**
     ID(0),
     CARERECORDID(1),
     PracticeCode(2),
     ODATE(3),
     READCODE(4),
     READTERM(5),
     NUMRESULT(6),
     NUMRESULT2(7);
     */

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

        //TODO - set asserter on Condition resource
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

        //TODO - must set the recorder on AllergyIntolerance resource
        //TODO - must set the substance on AllergyIntolerance resource

    }

    private static void addToMap(Resource resource, String careRecordId, Map<String, List<Resource>> fhirMap) throws TransformException {

        List<Resource> fhirResources = fhirMap.get(careRecordId);
        if (fhirResources == null){
            throw new TransformException("No patient resource for care record ID " + careRecordId);
        }

        fhirResources.add(resource);
    }

}
