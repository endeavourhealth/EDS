package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.List;

public class ObservationTransformer extends AbstractTransformer {

    public static void transform(Observation fhir, List<AbstractModel> models) throws Exception {

        Encounter model = new Encounter();


        model.setPatientId(transformPatientId(fhir.getSubject()));
        model.setEventDate(transformDate(fhir.getEffective()));

        CodeableConcept cc = fhir.getCode();
        for (Coding coding: cc.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                String value = coding.getCode();
                model.setSnomedConceptCode(Long.parseLong(value));
            } else {
                String value = coding.getCode();
                model.setNativeClinicalCode(value);
            }
        }

        if (fhir.hasValue()) {
            Quantity value = fhir.getValueQuantity();
            BigDecimal num = value.getValue();
            String unit = value.getUnit();
            model.setValue(new Double(num.doubleValue()));
            model.setUnits(unit);
        }

        if (fhir.hasPerformer()) {
            for (Reference ref: fhir.getPerformer()) {
                model.setStaffId(transformStaffId(ref));
            }
        }



//TODO - finish
        /**

         private Integer ageAtEvent;
         private Boolean isDiaryEvent;
         private Boolean isReferralEvent;
         private String consultationType;
         private Integer consultationDuration;
         private Integer problemId;
         */

        models.add(model);
    }
}
