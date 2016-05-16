package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.Event;
import org.endeavourhealth.core.transform.tpp.schema.Medication;
import org.endeavourhealth.core.transform.tpp.schema.Vaccination;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class VaccinationTransformer {

    public static void transform(List<Vaccination> tppVaccinations, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {
        for (Vaccination tppVaccination: tppVaccinations) {
            transform(tppVaccination, tppEvent, fhirEncounter, fhirResources);
        }
    }

    public static void transform(Vaccination tppVaccination, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {

        String batchNumber = tppVaccination.getBatchNumber();
        String notes = tppVaccination.getNotes();
        String name = tppVaccination.getName();
        String partType = tppVaccination.getPartType();
        String dosage = tppVaccination.getDosage();
        List<String> linkedProblemUID = tppVaccination.getLinkedProblemUID();

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_IMMUNIZATION));
        fhirResources.add(fhirImmunisation);

        fhirImmunisation.setPatient(Fhir.createPatientReference(fhirResources));
        fhirImmunisation.setPerformer(Fhir.createPractitionerReference(tppEvent.getUserName()));
        fhirImmunisation.setEncounter(Fhir.createEncounterReference(fhirEncounter));
        fhirImmunisation.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED.toCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);
        fhirImmunisation.setLotNumber(batchNumber);
        fhirImmunisation.addNote(Fhir.createAnnotation(notes));
        fhirImmunisation.setVaccineCode(Fhir.createCodeableConcept(name));
        fhirImmunisation.setDate(tppEvent.getDateTime().toGregorianCalendar().getTime());





    }
}
