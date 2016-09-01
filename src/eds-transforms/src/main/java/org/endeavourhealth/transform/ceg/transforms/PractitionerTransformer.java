package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Staff;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.List;

public class PractitionerTransformer extends AbstractTransformer {

    public static void transform(Practitioner fhir, List<AbstractModel> models) throws Exception {

        Staff model = new Staff();

        /*model.setPatientId(transformPatientId(fhir.getPatient()));
        model.setEventDate(transformDate(fhir.getOnset()));*/

//TODO - finish
        /**

         private String nativeClinicalCode;
         private Long staffId;
         private String consultationType;
         private Integer consultationDuration;
         private Integer problemId;
         private Long snomedConceptCode;

         */

        models.add(model);
    }
}
