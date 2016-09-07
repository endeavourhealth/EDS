package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ConditionTransformer extends AbstractTransformer {

    public static void transform(Condition fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        try {


            model.setPatientId(transformPatientId(fhir.getPatient()));
            model.setEventDate(transformDate(fhir.getOnset()));

            findClinicalCodesForEncounter(fhir.getCode(), model);
            findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);


            Reference practitionerReference = fhir.getAsserter();
            model.setStaffId(transformStaffId(practitionerReference));

            models.add(model);

        } catch (Exception ex) {
            System.out.print("ln");
        }

//TODO - finish
        /**
         private Integer problemId;

         */


    }
}
