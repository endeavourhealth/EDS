package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;
import org.endeavourhealth.transform.fhir.QuantityHelper;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Quantity;

public class ObservationPreTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {


        Observation parser = new Observation(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {

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
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }


}
