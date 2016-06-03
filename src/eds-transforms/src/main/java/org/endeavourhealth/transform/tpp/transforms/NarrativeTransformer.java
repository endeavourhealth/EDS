package org.endeavourhealth.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.tpp.schema.Event;
import org.endeavourhealth.transform.tpp.schema.Narrative;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class NarrativeTransformer {

    public static void transform(List<Narrative> tppNarratives, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) throws TransformException {
        for (Narrative tppNarrative: tppNarratives) {
            transform(tppNarrative, tppEvent, fhirEncounter, fhirResources);
        }
    }

    public static void transform(Narrative tppNarrative, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) throws TransformException {

        String line = tppNarrative.getLine();
        List<String> linkedProblemUIDs = tppNarrative.getLinkedProblemUID();
        //TODO - decide what to do with linked problem IDs

        Observation fhirObsrvation = new Observation();
        fhirObsrvation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));
        fhirResources.add(fhirObsrvation);

        fhirObsrvation.setStatus(Observation.ObservationStatus.FINAL);
        fhirObsrvation.setCode(CodeableConceptHelper.createCodeableConcept(line));

        fhirObsrvation.setSubject(ReferenceHelper.createReference(Patient.class, fhirResources));

        if (fhirEncounter != null) {
            String encounterId = fhirEncounter.getId();
            fhirObsrvation.setEncounter(ReferenceHelper.createReference(ResourceType.Encounter, encounterId));
        }

        XMLGregorianCalendar date = tppEvent.getDateTime();
        fhirObsrvation.setEffective(new DateTimeType(date.toGregorianCalendar().getTime()));

        String userName = tppEvent.getUserName();
        if (!Strings.isNullOrEmpty(userName)) {
            fhirObsrvation.addPerformer(ReferenceHelper.createReference(ResourceType.Encounter, userName));
        } else {
            //if we have no formal performer from the consultation, just fall back on using the organisation
            fhirObsrvation.addPerformer(ReferenceHelper.createReference(Organization.class, fhirResources));
        }
    }
}
