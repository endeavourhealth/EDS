package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ProcedureRequestTransformer extends AbstractTransformer {

    public static void transform(ProcedureRequest fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setDiaryEvent(Boolean.valueOf(true));
        model.setPatientId(transformPatientId(fhir.getSubject()));
        model.setEventDate(transformDate(fhir.getScheduledDateTimeType()));

        findClinicalCodesForEncounter(fhir.getCode(), model);
        findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);

        if (fhir.hasOrderer()) {
            Reference requesterReference = fhir.getOrderer();
            model.setStaffId(transformStaffId(requesterReference));
        }

//TODO - finish
        /**
         private Integer problemId;
         */

        models.add(model);
    }
}
