package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ImmunisationTransformer extends AbstractTransformer {

    public static void transform(Immunization fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setPatientId(transformPatientId(fhir.getPatient()));
        model.setEventDate(fhir.getDate());

        findClinicalCodesForEncounter(fhir.getVaccineCode(), model);
        findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);

        if (fhir.hasPerformer()) {
            Reference requesterReference = fhir.getPerformer();
            model.setStaffId(transformStaffId(requesterReference));
        }

//TODO - finish
        /**
         private Integer problemId;
         */

        models.add(model);
    }
}
