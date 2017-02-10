package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Nk1Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv1Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class PatientTransform {

    public static Patient fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        MshSegment sourceMsh = source.getMshSegment();
        PidSegment sourcePid = source.getPidSegment();
        Nk1Segment sourceNk1 = source.getNk1Segment();

        Patient target = new Patient();

        addIdentifiers(sourcePid, sourceMsh, target);

        addNames(sourcePid, target);

        setBirthAndDeath(sourcePid, target);

        if (!StringUtils.isEmpty(sourcePid.getSex()))
            target.setGender(getSex(sourcePid.getSex()));

        if (sourcePid.getHomeTelephones() != null)
            target.addTelecom(getContactPoint(sourcePid.getHomeTelephones()));

        if (sourcePid.getBusinessTelephones() != null)
            target.addTelecom(getContactPoint(sourcePid.getBusinessTelephones()));

        if (sourcePid.getAddresses() != null)
            target.addAddress(getAddresses(sourcePid.getAddresses()));

        if (source.hasNk1Segment())
            addPatientContact(sourceNk1, target);

        return target;
    }

    private static Enumerations.AdministrativeGender getSex(String gender) throws TransformException {
        return SexConverter.convert(gender);
    }

    private static void setBirthAndDeath(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfBirth() != null)
            target.setBirthDate(DateHelper.fromLocalDateTime(sourcePid.getDateOfBirth()));

        if (sourcePid.getDateOfDeath() != null)
            target.setDeceased(new DateTimeType(DateHelper.fromLocalDateTime(sourcePid.getDateOfDeath())));
        else if (isDeceased(sourcePid.getDeathIndicator()))
            target.setDeceased(new BooleanType(true));
    }

    private static boolean isDeceased(String deathIndicator) throws TransformException {
        if (StringUtils.isEmpty(deathIndicator))
            return false;

        String indicator = deathIndicator.trim().toLowerCase().substring(0, 1);

        if (indicator.equals("y"))
            return true;
        else if (indicator.equals("n"))
            return false;

        throw new TransformException(indicator + " not recognised as a death indicator");
    }

    private static Address getAddresses(List<Xad> addresses) throws TransformException {
        for (Xad xad : addresses)
            if (xad != null)
                return AddressConverter.convert(xad);

        return null;
    }

    private static void addNames(PidSegment sourcePid, Patient target) throws TransformException {
        target.addName(getNames(sourcePid.getPatientNames()));
        target.addName(getNames(sourcePid.getPatientAlias()));
    }

    private static HumanName getNames(List<Xpn> name) throws TransformException {
        for (Xpn xpn : name)
            if (xpn != null)
                return NameConverter.convert(xpn);

        return null;
    }

    private static void addIdentifiers(PidSegment sourcePid, MshSegment sourceMsh, Patient target) {
        addIdentifier(target, sourcePid.getExternalPatientId(), sourceMsh.getSendingFacility());

        for (Cx cx : sourcePid.getInternalPatientId())
            addIdentifier(target, cx, sourceMsh.getSendingFacility());

        addIdentifier(target, sourcePid.getAlternatePatientId(), sourceMsh.getSendingFacility());
    }

    private static void addIdentifier(Patient target, Cx cx, String sendingFacility) {
        Identifier identifier = IdentifierConverter.convert(cx, sendingFacility);

        if (identifier != null)
            target.addIdentifier(identifier);
    }

    private static void addPatientContact(Nk1Segment sourceNk1, Patient target) throws TransformException {
        Patient.ContactComponent contactComponent = new Patient.ContactComponent();

        contactComponent.setName(getNames(sourceNk1.getNKName()));

        contactComponent.addRelationship(getRelationship(sourceNk1.getRelationship()));

        if (sourceNk1.getPhoneNumber() != null)
            contactComponent.addTelecom(getContactPoint(sourceNk1.getPhoneNumber()));

        if (sourceNk1.getBusinessPhoneNumber() != null)
            contactComponent.addTelecom(getContactPoint(sourceNk1.getBusinessPhoneNumber()));

        if (sourceNk1.getAddress() != null)
            contactComponent.setAddress(getAddresses(sourceNk1.getAddress()));

        if (!StringUtils.isEmpty(sourceNk1.getSex()))
            contactComponent.setGender(getSex(sourceNk1.getSex()));

        target.addContact(contactComponent);

    }

    private static ContactPoint getContactPoint(List<Xtn> contact) throws TransformException {
        for (Xtn xtn : contact)
            if (xtn != null)
                return TelecomConverter.convert(xtn);

        return null;
    }

    private static CodeableConcept getRelationship(Ce ce) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding();
        codeableConcept.setText(ce.getAsString());

        return codeableConcept;
    }

}
