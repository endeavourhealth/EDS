package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ReferralRequestTransformer extends AbstractTransformer {

    public static void transform(ReferralRequest fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setReferralEvent(Boolean.valueOf(true));
        model.setPatientId(transformPatientId(fhir.getPatient()));
        model.setEventDate(fhir.getDate());

        findClinicalCodesForEncounter(fhir.getType(), model);
        findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);

        if (fhir.hasRequester()) {
            Reference requesterReference = fhir.getRequester();
            model.setStaffId(transformStaffId(requesterReference));
        }

//TODO - finish
        /**
         private Integer problemId;

         */

        models.add(model);
    }
}
