package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.common.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
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

        fhirImmunisation.setPatient(findAndCreatePatientReference(fhirResources));
        fhirImmunisation.setPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, tppEvent.getUserName()));
        fhirImmunisation.setEncounter(createReferenceExternal(fhirEncounter));
        fhirImmunisation.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED.toCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);
        fhirImmunisation.setLotNumber(batchNumber);
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(notes));
        fhirImmunisation.setVaccineCode(CodeableConceptHelper.createCodeableConcept(name));
        fhirImmunisation.setDate(tppEvent.getDateTime().toGregorianCalendar().getTime());
    }

    private static Reference findAndCreatePatientReference(List<Resource> fhirResources) throws TransformException {
        try {
            return ReferenceHelper.findAndCreateReference(Patient.class, fhirResources);
        } catch (org.endeavourhealth.common.exceptions.TransformException e) {
            throw new TransformException("Could not create patient reference, see cause", e);
        }
    }

    private static Reference createReferenceExternal(Resource resource) throws TransformException {
        try {
            return ReferenceHelper.createReferenceExternal(resource);
        } catch (org.endeavourhealth.common.exceptions.TransformException e) {
            throw new TransformException("Could not create external reference, see cause", e);
        }
    }
}
