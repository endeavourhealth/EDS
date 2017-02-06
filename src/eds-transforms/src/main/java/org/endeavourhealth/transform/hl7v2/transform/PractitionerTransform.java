package org.endeavourhealth.transform.hl7v2.transform;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.EvnSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.ArrayList;
import java.util.List;

public class PractitionerTransform {
    public static List<Practitioner> fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        List<Practitioner> practitioners = new ArrayList<>();

        EvnSegment evnSegment = source.getEvnSegment();

        for (Xcn xcn : evnSegment.getOperators())
            practitioners.add(transform(xcn));

        return practitioners;
    }

    private static Practitioner transform(Xcn xcn) throws TransformException {
        if (xcn == null)
            return null;

        Practitioner practitioner = new Practitioner();

        practitioner.setName(NameConverter.convert(xcn));

        return practitioner;
    }
}
