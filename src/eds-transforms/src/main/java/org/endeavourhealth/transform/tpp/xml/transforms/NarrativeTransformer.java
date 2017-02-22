package org.endeavourhealth.transform.tpp.xml.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.transform.tpp.xml.schema.Event;
import org.endeavourhealth.transform.tpp.xml.schema.Narrative;
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

        fhirObsrvation.setSubject(findAndCreateReference(Patient.class, fhirResources));

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
            fhirObsrvation.addPerformer(findAndCreateReference(Organization.class, fhirResources));
        }
    }

    private static Reference findAndCreateReference(Class<? extends Resource> resourceClass, List<Resource> fhirResources) throws TransformException {
        try {
            return ReferenceHelper.findAndCreateReference(resourceClass, fhirResources);
        } catch (org.endeavourhealth.common.exceptions.TransformException e) {
            throw new TransformException("Error creating reference, see cause", e);
        }
    }
}
