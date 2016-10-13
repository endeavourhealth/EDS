package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.Date;
import java.util.Map;

public class ObservationPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        Observation parser = (Observation)parsers.get(Observation.class);

        while (parser.nextRecord()) {

            try {
                processLine(parser, csvHelper, csvProcessor, version);
            } catch (Exception ex) {
                throw new TransformException(parser.getCurrentState().toString(), ex);
            }
        }
    }

    private static void processLine(Observation parser, EmisCsvHelper csvHelper, CsvProcessor csvProcessor, String version) throws Exception {

        //the code ID should NEVER be null, but the test data has nulls, so adding this to handle those rows gracefully
        if (version.equalsIgnoreCase(EmisCsvTransformer.VERSION_TEST_PACK)
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
                    CodeableConcept codeableConcept = csvHelper.findClinicalCode(codeId, csvProcessor);

                    org.hl7.fhir.instance.model.Observation.ObservationComponentComponent component = new org.hl7.fhir.instance.model.Observation.ObservationComponentComponent();
                    component.setCode(codeableConcept);
                    component.setValue(quantity);

                    csvHelper.cacheBpComponent(parentGuid, patientGuid, component);
                }
            }
        }

        String problemGuid = parser.getProblemUGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {

            //if this record is linked to a problem, store this relationship in the helper
            String observationGuid = parser.getObservationGuid();
            String patientGuid = parser.getPatientGuid();
            ResourceType resourceType = ObservationTransformer.getTargetResourceType(parser, csvProcessor, csvHelper);

            csvHelper.cacheProblemRelationship(problemGuid,
                    patientGuid,
                    observationGuid,
                    resourceType);
        }

        Long codeId = parser.getCodeId();
        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId, csvProcessor);
        if (codeType == ClinicalCodeType.Ethnicity) {

            Date effectiveDate = parser.getEffectiveDate();
            String effectiveDatePrecision = parser.getEffectiveDatePrecision();
            DateTimeType fhirDate = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);


        } else if (codeType == ClinicalCodeType.Marital_Status) {

            Date effectiveDate = parser.getEffectiveDate();
            String effectiveDatePrecision = parser.getEffectiveDatePrecision();
            DateTimeType fhirDate = EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision);


        }
    }

}
