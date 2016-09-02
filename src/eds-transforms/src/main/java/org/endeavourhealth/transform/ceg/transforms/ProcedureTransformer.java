package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ProcedureTransformer extends AbstractTransformer {

    public static void transform(Procedure fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setPatientId(transformPatientId(fhir.getSubject()));
        model.setEventDate(transformDate(fhir.getPerformed()));

        findClinicalCodesForEncounter(fhir.getCode(), model);
        findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);

        if (fhir.hasPerformer()) {
            Procedure.ProcedurePerformerComponent performer = fhir.getPerformer().get(0);
            Reference practitionReference = performer.getActor();
            model.setStaffId(transformStaffId(practitionReference));
        }

//TODO - finish
        /**
         private Integer problemId;

         */

        models.add(model);
    }
}
