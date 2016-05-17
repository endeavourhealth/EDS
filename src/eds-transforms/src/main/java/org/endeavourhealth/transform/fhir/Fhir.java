package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public final class Fhir {

    public static Identifier createNhsNumberIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, value, FhirUris.IDENTIFIER_SYSTEM_NHSNUMBER);
    }
    public static Identifier createOdsOrganisationIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, value, FhirUris.IDENTIFIER_SYSTEM_ODS_CODE);
    }

    public static Identifier createIdentifier(Identifier.IdentifierUse use, String value, String system) {

        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return new Identifier()
                .setUse(use)
                .setValue(value)
                .setSystem(system);
    }

    public static HumanName createHumanName(HumanName.NameUse use, String title, String firstName, String middleNames, String surname) {
        HumanName ret = new HumanName();
        ret.setUse(use);

        //build up full name in standard format; SURNAME, firstname (title)
        StringBuilder displayName = new StringBuilder();

        if (!Strings.isNullOrEmpty(surname)) {
            List<String> v = split(surname);
            v.forEach(ret::addFamily);

            displayName.append(surname.toUpperCase());
        }

        if (!Strings.isNullOrEmpty(firstName)) {
            List<String> v = split(firstName);
            v.forEach(ret::addGiven);

            displayName.append(", ");
            displayName.append(firstName);
        }

        if (!Strings.isNullOrEmpty(middleNames)) {
            List<String> v = split(middleNames);
            v.forEach(ret::addGiven);
        }

        if (!Strings.isNullOrEmpty(title)) {
            List<String> v = split(title);
            v.forEach(ret::addPrefix);

            displayName.append(" (");
            displayName.append(title);
            displayName.append(")");
        }

        ret.setText(displayName.toString());

        return ret;
    }
    private static List<String> split(String s) {
        return Arrays.asList(s.split(" "));
    }

    public static CodeableConcept createCodeableConcept(String system, String term, String code) {
        return new CodeableConcept()
                .addCoding(new Coding()
                    .setSystem(system)
                    .setDisplay(term)
                    .setCode(code));
    }

    public static CodeableConcept createCodeableConcept(String text) {
        return new CodeableConcept()
                .setText(text);
    }

    public static ContactPoint createContactPointIfRequired(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use,
                                                    String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return createContactPoint(system, use, value);
    }
    public static ContactPoint createContactPoint(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use, String value) {

        return new ContactPoint()
                .setSystem(system)
                .setUse(use)
                .setValue(value);
    }

    public static Reference createReference(ResourceType resourceType, String value) throws TransformException {
        if (Strings.isNullOrEmpty(value)) {
            throw new TransformException("Missing value when creating " + resourceType + "referece");
        }

        String reference = createResourceReference(resourceType, value);
        return new Reference().setReference(reference);
    }
    public static String createResourceReference(ResourceType resourceType, String id) {
        return resourceType.toString() + "/" + id;
    }

    public static Practitioner findPractitionerForId(List<Resource> fhirResources, String id) throws TransformException {

        Optional<Practitioner> ret = fhirResources
                .stream()
                .filter(t -> t instanceof Practitioner && ((Practitioner)t).getId().equals(id))
                .map(t -> (Practitioner)t)
                .findFirst();

        if (!ret.isPresent()) {
            throw new TransformException("Failed to find practitioner for ID " + id);
        }

        return ret.get();
    }

    public static String findOrganisationId(List<Resource> fhirResources) throws TransformException {
        return findOrganisation(fhirResources).getId();
    }
    public static Organization findOrganisation(List<Resource> fhirResources) throws TransformException {

        Optional<Organization> ret = fhirResources
                .stream()
                .filter(t -> t instanceof Organization)
                .map(t -> (Organization)t)
                .findFirst();

        if (!ret.isPresent()) {
            throw new TransformException("Failed to find organisation resource");
        }

        return ret.get();
    }

    public static Location findLocationForName(List<Resource> fhirResources, String name) throws TransformException {

        Optional<Location> ret = fhirResources
                .stream()
                .filter(t -> t instanceof Location && ((Location)t).getName().equals(name))
                .map(t -> (Location)t)
                .findFirst();

        if (!ret.isPresent()) {
            throw new TransformException("Failed to find location for name " + name);
        }

        return ret.get();
    }

    public static String findPatientId(List<Resource> fhirResources) throws TransformException {
        return findPatient(fhirResources).getId();
    }
    public static Patient findPatient(List<Resource> fhirResources) throws TransformException {
        Optional<Patient> ret = fhirResources
                .stream()
                .filter(t -> t instanceof Patient)
                .map(t -> (Patient)t)
                .findFirst();

        if (!ret.isPresent()) {
            throw new TransformException("Failed to find patient resource");
        }

        return ret.get();
    }

    public static Extension createExtension(String uri, Type value) {
        return new Extension()
                .setUrl(uri)
                .setValue(value);
    }

    public static Appointment.AppointmentParticipantComponent createParticipant(ResourceType resourceType, String id) throws TransformException {
        return createParticipant(resourceType, id, Appointment.ParticipantRequired.REQUIRED, Appointment.ParticipationStatus.ACCEPTED);
    }
    public static Appointment.AppointmentParticipantComponent createParticipant(ResourceType resourceType, String id, Appointment.ParticipantRequired required, Appointment.ParticipationStatus status) throws TransformException {
        return new Appointment.AppointmentParticipantComponent()
                .setActor(Fhir.createReference(resourceType, id))
                .setRequired(required)
                .setStatus(status);
    }

    public static Reference createPatientReference(List<Resource> fhirResources) throws TransformException{
        String patientId = findPatientId(fhirResources);
        return createReference(ResourceType.Patient, patientId);
    }
    public static Reference createPatientReference(Patient fhirPatient) throws TransformException {
        String patientId = fhirPatient.getId();
        return createReference(ResourceType.Patient, patientId);
    }

    public static Reference createPractitionerReference(String practitionerId) throws TransformException {
        if (Strings.isNullOrEmpty(practitionerId)) {
            return null;
        }

        return createReference(ResourceType.Practitioner, practitionerId);
    }
    public static Reference createEncounterReference(Encounter fhirEncounter) throws TransformException {
        if (fhirEncounter == null) {
            return null;
        }
        String encounterId = fhirEncounter.getId();
        return createReference(ResourceType.Encounter, encounterId);
    }
    public static Annotation createAnnotation(String notes) {
        if (Strings.isNullOrEmpty(notes)) {
            return null;
        }

        return new Annotation().setText(notes);
    }

    public static Period createPeriod(XMLGregorianCalendar xmlStart, XMLGregorianCalendar xmlEnd) {
        Date start = null;
        if (xmlStart != null) {
            start = xmlStart.toGregorianCalendar().getTime();
        }
        Date end = null;
        if (xmlEnd != null) {
            end = xmlEnd.toGregorianCalendar().getTime();
        }
        return createPeriod(start, end);
    }
    public static Period createPeriod(Date start, Date end) {
        Period fhirPeriod = new Period();
        fhirPeriod.setStart(start);
        fhirPeriod.setEnd(end);
        return fhirPeriod;
    }

    public static boolean isActive(EpisodeOfCare episodeOfCare) {
        return isActive(episodeOfCare.getPeriod());
    }
    public static boolean isActive(Period period) {
        return (period.getEnd() == null
                || period.getEnd().after(new Date()));
    }
}
