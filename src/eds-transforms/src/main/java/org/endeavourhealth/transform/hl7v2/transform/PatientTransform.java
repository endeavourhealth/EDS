package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Cx;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xad;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xpn;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.hl7.fhir.instance.model.*;

public class PatientTransform {

    public static Patient fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        MshSegment sourceMsh = source.getMshSegment();
        PidSegment sourcePid = source.getPidSegment();

        Patient target = new Patient();

        addIdentifiers(sourcePid, sourceMsh, target);

        addNames(sourcePid, target);

        setBirthAndDeath(sourcePid, target);

        setSex(sourcePid, target);

        addAddresses(sourcePid, target);

        return target;
    }

    private static void setSex(PidSegment sourcePid, Patient target) throws TransformException {
        if (StringUtils.isEmpty(sourcePid.getSex()))
            return;

        target.setGender(SexConverter.convert(sourcePid.getSex()));
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
