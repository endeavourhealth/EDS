package org.endeavourhealth.transform.hl7v2.transform;

import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.EvnSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pd1Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv1Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.CodeableConceptHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.IdentifierConverter;
import org.endeavourhealth.transform.hl7v2.transform.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class PractitionerTransform {
    public static List<Practitioner> fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        List<Practitioner> practitioners = new ArrayList<>();

        MshSegment mshSegment = source.getMshSegment();
        EvnSegment evnSegment = source.getEvnSegment();
        Pd1Segment pd1Segment = source.getPd1Segment();
        Pv1Segment pv1Segment = source.getPv1Segment();

        for (Xcn xcn : evnSegment.getOperators())
            practitioners.add(transform(xcn, mshSegment.getSendingFacility()));

        for (Xcn xcn : pd1Segment.getPatientPrimaryCareProvider())
            practitioners.add(transform(xcn, mshSegment.getSendingFacility()));

        for (Xcn xcn : pv1Segment.getAttendingDoctor())
            practitioners.add(transform(xcn, mshSegment.getSendingFacility()));

        for (Xcn xcn : pv1Segment.getReferringDoctor())
            practitioners.add(transform(xcn, mshSegment.getSendingFacility()));

        for (Xcn xcn : pv1Segment.getConsultingDoctor())
            practitioners.add(transform(xcn, mshSegment.getSendingFacility()));

        return practitioners;
    }

    public static List<Encounter.EncounterParticipantComponent> createParticipantComponents(List<Xcn> practitioners, String type) throws ParseException, TransformException {
        List<Encounter.EncounterParticipantComponent> participantComponentList = new ArrayList<>();
        for (Xcn xcn : practitioners)
            participantComponentList.add(createParticipantComponent(xcn, type));

        return participantComponentList;
    }

    private static Encounter.EncounterParticipantComponent createParticipantComponent(Xcn xcn, String type) throws TransformException {
        Encounter.EncounterParticipantComponent epl = new Encounter.EncounterParticipantComponent();
        epl.addType(CodeableConceptHelper.getCodeableConceptFromString(type));
        Reference reference = new Reference();
        reference.setReference(xcn.getId());

        epl.setIndividual(reference);

        return epl;
    }

    private static Practitioner transform(Xcn xcn, String sendingFacility) throws TransformException {
        if (xcn == null)
            return null;

        Practitioner practitioner = new Practitioner();

        practitioner.setName(NameConverter.convert(xcn));

        Identifier identifier = IdentifierConverter.convert(xcn, sendingFacility);

        if (identifier != null)
            practitioner.addIdentifier(identifier);

        return practitioner;
    }
}
