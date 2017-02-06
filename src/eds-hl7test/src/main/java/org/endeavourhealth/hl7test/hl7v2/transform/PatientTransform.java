package org.endeavourhealth.hl7test.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7test.hl7v2.parser.Helpers;
import org.endeavourhealth.hl7test.hl7v2.parser.ParseException;
import org.endeavourhealth.hl7test.hl7v2.parser.datatypes.Cx;
import org.endeavourhealth.hl7test.hl7v2.parser.datatypes.Xpn;
import org.endeavourhealth.hl7test.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.hl7test.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.hl7test.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.hl7test.hl7v2.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;

public class PatientTransform {
    private static final String IDENTIFIER_SYSTEM_HL7V2_ASSIGNING_AUTHORITY = "http://endeavourhealth.org/fhir/v2-identifier-assigning-authority/{0}/{1}";

    public static Patient fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        MshSegment sourceMsh = source.getMshSegment();
        PidSegment sourcePid = source.getPidSegment();

        Patient target = new Patient();

        addIdentifiers(sourcePid, sourceMsh, target);

        addNames(sourcePid, target);




        return target;
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

    private static void addIdentifier(Patient target, Cx source, String sendingFacility) {
        if (source == null)
            return;

        target.addIdentifier(new Identifier()
                .setValue(source.getId())
                .setSystem(Helpers.formatString(IDENTIFIER_SYSTEM_HL7V2_ASSIGNING_AUTHORITY, sendingFacility, source.getIdentifierTypeCode())));
    }

}
