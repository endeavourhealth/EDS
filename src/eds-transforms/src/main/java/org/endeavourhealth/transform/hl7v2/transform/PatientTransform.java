package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Cx;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xad;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xpn;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.AddressConverter;
import org.endeavourhealth.transform.hl7v2.transform.converters.DateHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.IdentifierConverter;
import org.endeavourhealth.transform.hl7v2.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.BooleanType;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;

import java.time.LocalDateTime;

public class PatientTransform {

    public static Patient fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        MshSegment sourceMsh = source.getMshSegment();
        PidSegment sourcePid = source.getPidSegment();

        Patient target = new Patient();

        addIdentifiers(sourcePid, sourceMsh, target);

        addNames(sourcePid, target);

        if (sourcePid.getDateOfBirth() != null)
            target.setBirthDate(DateHelper.fromLocalDateTime(sourcePid.getDateOfBirth()));

        if (sourcePid.getDateOfDeath() != null)
            target.setDeceased(new DateTimeType(DateHelper.fromLocalDateTime(sourcePid.getDateOfDeath())));
        else if (isDeceased(sourcePid.getDeathIndicator()))
            target.setDeceased(new BooleanType(true));



        addAddresses(sourcePid, target);



        return target;
    }

    private static boolean isDeceased(String deathIndicator) {
        if (StringUtils.isNotEmpty(deathIndicator))
            return (deathIndicator.trim().toLowerCase().substring(0, 1).equals("y"));

        return false;
    }

    private static void addAddresses(PidSegment sourcePid, Patient target) throws TransformException {
        for (Xad xad : sourcePid.getAddresses())
            if (xad != null)
                target.addAddress(AddressConverter.convert(xad));
    }

    private static void addNames(PidSegment sourcePid, Patient target) throws TransformException {
        for (Xpn xpn : sourcePid.getPatientNames())
            if (xpn != null)
                target.addName(NameConverter.convert(xpn));

        for (Xpn xpn : sourcePid.getPatientAlias())
            if (xpn != null)
                target.addName(NameConverter.convert(xpn));
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
}
