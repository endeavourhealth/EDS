package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.tpp.xml.schema.Event;
import org.endeavourhealth.transform.tpp.xml.schema.Vaccination;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class VaccinationTransformer {

    public static void transform(List<Vaccination> tppVaccinations, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) throws TransformException {
        for (Vaccination tppVaccination: tppVaccinations) {
            transform(tppVaccination, tppEvent, fhirEncounter, fhirResources);
        }
    }

    public static void transform(Vaccination tppVaccination, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) throws TransformException {

        String batchNumber = tppVaccination.getBatchNumber();
        String notes = tppVaccination.getNotes();
        String name = tppVaccination.getName();
        String partType = tppVaccination.getPartType();
        String dosage = tppVaccination.getDosage();
        List<String> linkedProblemUID = tppVaccination.getLinkedProblemUID();

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));
        fhirResources.add(fhirImmunisation);

        fhirImmunisation.setPatient(ReferenceHelper.findAndCreateReference(Patient.class, fhirResources));
        fhirImmunisation.setPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, tppEvent.getUserName()));
        fhirImmunisation.setEncounter(ReferenceHelper.createReferenceExternal(fhirEncounter));
        fhirImmunisation.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED.toCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);
        fhirImmunisation.setLotNumber(batchNumber);
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(notes));
        fhirImmunisation.setVaccineCode(CodeableConceptHelper.createCodeableConcept(name));
        fhirImmunisation.setDate(tppEvent.getDateTime().toGregorianCalendar().getTime());





    }
}
