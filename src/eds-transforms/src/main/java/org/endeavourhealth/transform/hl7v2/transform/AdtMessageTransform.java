package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class AdtMessageTransform {
    public static Bundle transform(AdtMessage sourceMessage, CodeMapper codeMapper) throws Exception {
        Validate.notNull(sourceMessage);

        List<Resource> targetResources = new ArrayList<>();

        targetResources.add(MessageHeaderTransform.fromHl7v2(sourceMessage.getMshSegment()));
        targetResources.add(PatientTransform.fromHl7v2(sourceMessage));

        if (sourceMessage.hasPv1Segment())
            targetResources.add(PatientVisitTransform.fromHl7v2(sourceMessage.getPv1Segment()));

        for (Practitioner practitioner : PractitionerTransform.fromHl7v2(sourceMessage))
            targetResources.add(practitioner);

        if (sourceMessage.hasPv1Segment()) {
            if (sourceMessage.getPv1Segment().getAssignedPatientLocation() != null) {
                addLocations(sourceMessage.getPv1Segment().getAssignedPatientLocation(), targetResources);
            }

            if (sourceMessage.getPv1Segment().getPriorPatientLocation() != null) {
                addLocations(sourceMessage.getPv1Segment().getPriorPatientLocation(), targetResources);
            }
        }
        return createBundle(targetResources);
    }

    private static void addLocations(Pl location, List<Resource> targetResources) throws TransformException {
        List<Location> locations = LocationTransform.convert(location);
        for (Location loc : locations)
            targetResources.add(loc);
    }

    private static Bundle createBundle(List<Resource> resources) {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
