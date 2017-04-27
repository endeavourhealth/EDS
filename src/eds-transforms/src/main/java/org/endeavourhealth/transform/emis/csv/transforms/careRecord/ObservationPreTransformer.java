package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.QuantityHelper;
import org.endeavourhealth.common.fhir.schema.EthnicCategory;
import org.endeavourhealth.common.fhir.schema.MaritalStatus;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.Map;

public class ObservationPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        AbstractCsvParser parser = parsers.get(Observation.class);
        while (parser.nextRecord()) {

            try {
                processLine((Observation)parser, csvHelper, fhirResourceFiler, version);
            } catch (Exception ex) {
                throw new TransformException(parser.getCurrentState().toString(), ex);
            }
        }
    }


    private static void processLine(Observation parser, EmisCsvHelper csvHelper, FhirResourceFiler fhirResourceFiler, String version) throws Exception {

        if (parser.getDeleted()) {
            return;
        }

        //the test pack has non-deleted rows with missing CodeIds, so skip these rows
        if ((version.equals(EmisCsvToFhirTransformer.VERSION_5_0)
                || version.equals(EmisCsvToFhirTransformer.VERSION_5_1))
            && parser.getCodeId() == null) {
            return;
        }

        String parentGuid = parser.getParentObservationGuid();
        if (!Strings.isNullOrEmpty(parentGuid)) {

            String observationGuid = parser.getObservationGuid();
            String patientGuid = parser.getPatientGuid();

            //if the observation links to a parent observation, store this relationship in the
            //helper class, so when processing later, we can set the Has Member reference in the FHIR observation
            csvHelper.cacheObservationParentRelationship(parentGuid, patientGuid, observationGuid);

            //if the observation is a BP reading, then cache in the helper
            String unit = parser.getNumericUnit();
            if (!Strings.isNullOrEmpty(unit)) {
                unit = unit.trim();

                //BP readings uniquely use mmHg for the units, so detect them using that
                if (unit.equalsIgnoreCase("mmHg")
                        || unit.equalsIgnoreCase("mm Hg")) {

                    Long codeId = parser.getCodeId();
                    Double value = parser.getValue();
                    Quantity quantity = QuantityHelper.createQuantity(value, unit);
                    CodeableConcept codeableConcept = csvHelper.findClinicalCode(codeId);

                    org.hl7.fhir.instance.model.Observation.ObservationComponentComponent component = new org.hl7.fhir.instance.model.Observation.ObservationComponentComponent();
                    component.setCode(codeableConcept);
                    component.setValue(quantity);

                    csvHelper.cacheBpComponent(parentGuid, patientGuid, component);
                }
            }
        }

        String problemGuid = parser.getProblemGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {

            //if this record is linked to a problem, store this relationship in the helper
            String observationGuid = parser.getObservationGuid();
            String patientGuid = parser.getPatientGuid();
            ResourceType resourceType = ObservationTransformer.getTargetResourceType(parser, csvHelper);

            csvHelper.cacheProblemRelationship(problemGuid,
                    patientGuid,
                    observationGuid,
                    resourceType);
        }

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            String observationGuid = parser.getObservationGuid();
            String patientGuid = parser.getPatientGuid();
            ResourceType resourceType = ObservationTransformer.getTargetResourceType(parser, csvHelper);

            csvHelper.cacheConsultationRelationship(consultationGuid,
                    patientGuid,
                    observationGuid,
                    resourceType);
        }

        Long codeId = parser.getCodeId();
        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId);
        if (codeType == ClinicalCodeType.Ethnicity) {

            Date effectiveDate = parser.getEffectiveDate();
            String effectiveDatePrecision = parser.getEffectiveDatePrecision();
            DateTimeType fhirDate = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);

            CodeableConcept codeableConcept = csvHelper.findClinicalCode(codeId);
            EthnicCategory ethnicCategory = findEthnicityCode(codeableConcept);
            if (ethnicCategory != null) {

                String patientGuid = parser.getPatientGuid();
                csvHelper.cacheEthnicity(patientGuid, fhirDate, ethnicCategory);
            }

        } else if (codeType == ClinicalCodeType.Marital_Status) {

            Date effectiveDate = parser.getEffectiveDate();
            String effectiveDatePrecision = parser.getEffectiveDatePrecision();
            DateTimeType fhirDate = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);

            CodeableConcept codeableConcept = csvHelper.findClinicalCode(codeId);
            MaritalStatus maritalStatus = findMaritalStatus(codeableConcept);
            if (maritalStatus != null) {

                String patientGuid = parser.getPatientGuid();
                csvHelper.cacheMaritalStatus(patientGuid, fhirDate, maritalStatus);
            }
        }

        //if we've previously found that our observation is a problem (via the problem pre-transformer)
        //then we need to cache the code ID of our observation
        String patientGuid = parser.getPatientGuid();
        String observationGuid = parser.getObservationGuid();
        if (csvHelper.isProblemObservationGuid(patientGuid, observationGuid)) {
            CodeableConcept codeableConcept = csvHelper.findClinicalCode(codeId);
            String readCode = CodeableConceptHelper.findOriginalCode(codeableConcept);
            csvHelper.cacheProblemObservationGuid(patientGuid, observationGuid, readCode);
        }
    }

    private static MaritalStatus findMaritalStatus(CodeableConcept codeableConcept) {
        String code = findRead2Code(codeableConcept);
        if (code == null) {
            return null;
        }

        if (code.equals("1331.")) {
            //single

        } else if (code.equals("1332.")
            || code.equals("EMISNQHO15")
            || code.equals("EMISNQHO16")
            || code.equals("133S.")) {
            return MaritalStatus.MARRIED;

        } else if (code.equals("1334.")
            || code.equals("133T.")) {
            return MaritalStatus.DIVORCED;

        } else if (code.equals("1335.")
            || code.equals("133C.")
            || code.equals("133V.")) {
            return MaritalStatus.WIDOWED;

        } else if (code.equals("1333.")) {
            return MaritalStatus.LEGALLY_SEPARATED;

        } else if (code.equals("1336.")
            || code.equals("133e.")
                || code.equals("133G.")
                || code.equals("133H.")
                || code.equals("EMISNQCO47")) {
            return MaritalStatus.DOMESTIC_PARTNER;

        }

        return null;
    }

    private static EthnicCategory findEthnicityCode(CodeableConcept codeableConcept) {
        String code = findRead2Code(codeableConcept);
        if (code == null) {
            return null;
        }

        if (code.startsWith("9i0")) {
            return EthnicCategory.WHITE_BRITISH;
        } else if (code.startsWith("9i1")) {
            return EthnicCategory.WHITE_IRISH;
        } else if (code.startsWith("9i2")) {
            return EthnicCategory.OTHER_WHITE;
        } else if (code.startsWith("9i3")) {
            return EthnicCategory.MIXED_CARIBBEAN;
        } else if (code.startsWith("9i4")) {
            return EthnicCategory.MIXED_AFRICAN;
        } else if (code.startsWith("9i5")) {
            return EthnicCategory.MIXED_ASIAN;
        } else if (code.startsWith("9i6")) {
            return EthnicCategory.OTHER_MIXED;
        } else if (code.startsWith("9i7")) {
            return EthnicCategory.ASIAN_INDIAN;
        } else if (code.startsWith("9i8")) {
            return EthnicCategory.ASIAN_PAKISTANI;
        } else if (code.startsWith("9i9")) {
            return EthnicCategory.ASIAN_BANGLADESHI;
        } else if (code.startsWith("9iA")) {
            return EthnicCategory.OTHER_ASIAN;
        } else if (code.startsWith("9iB")) {
            return EthnicCategory.BLACK_CARIBBEAN;
        } else if (code.startsWith("9iC")) {
            return EthnicCategory.BLACK_AFRICAN;
        } else if (code.startsWith("9iD")) {
            return EthnicCategory.OTHER_BLACK;
        } else if (code.startsWith("9iE")) {
            return EthnicCategory.CHINESE;
        } else if (code.startsWith("9iF")) {
            return EthnicCategory.OTHER;
        } else if (code.startsWith("9iG")) {
            return EthnicCategory.NOT_STATED;
        } else {
            return null;
        }
    }

    private static String findRead2Code(CodeableConcept codeableConcept) {
        for (Coding coding: codeableConcept.getCoding()) {

            //would prefer to check for procedures using Snomed, but this Read2 is simple and works
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMIS_CODE)) {
                return coding.getCode();
            }
        }

        return null;
    }
}


