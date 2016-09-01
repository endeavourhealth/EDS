package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Procedure;

import java.util.List;

public class ProcedureTransformer extends AbstractTransformer {

    public static void transform(Procedure fhir, List<AbstractModel> models) throws Exception {

        Encounter model = new Encounter();

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
