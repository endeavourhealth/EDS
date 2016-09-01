package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class FamilyMemberHistoryTransformer extends AbstractTransformer {

    public static void transform(FamilyMemberHistory fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setPatientId(transformPatientId(fhir.getPatient()));
        model.setEventDate(fhir.getDate());

        if (fhir.hasCondition()) {
            FamilyMemberHistory.FamilyMemberHistoryConditionComponent condition = fhir.getCondition().get(0);
            findClinicalCodesForEncounter(condition.getCode(), model);
        }

        Reference encounterReference = (Reference)findExtension(fhir, FhirExtensionUri.ASSOCIATED_ENCOUNTER);
        findConsultationDetailsForEncounter(encounterReference, hsAllResources, model);

        Reference practitionerReference = (Reference)findExtension(fhir, FhirExtensionUri.RECORDED_BY);
        model.setStaffId(transformStaffId(practitionerReference));

//TODO - finish
        /**
         private Integer problemId;

         */

        models.add(model);
    }
}
