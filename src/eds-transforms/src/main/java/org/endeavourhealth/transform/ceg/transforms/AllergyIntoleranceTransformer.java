package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AllergyIntoleranceTransformer extends AbstractTransformer {


    public static void transform(AllergyIntolerance fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setPatientId(transformPatientId(fhir.getPatient()));

        Date date = fhir.getRecordedDate();
        model.setEventDate(date);

        findClinicalCodesForEncounter(fhir.getSubstance(), model);

        Reference encounterReference = (Reference)findExtension(fhir, FhirExtensionUri.ASSOCIATED_ENCOUNTER);
        findConsultationDetailsForEncounter(encounterReference, hsAllResources, model);

        Reference practitionerReference = fhir.getRecorder();
        model.setStaffId(transformStaffId(practitionerReference));

        //TODO - finish allergies

        /**
         private Integer problemId;
         */

        models.add(model);

    }
}
