package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.transform.converters.LocationConverter;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class AdtMessageTransform {
    public static Bundle transform(AdtMessage sourceMessage) throws Exception {
        Validate.notNull(sourceMessage);

        List<Resource> targetResources = new ArrayList<>();

        targetResources.add(MessageHeaderTransform.fromHl7v2(sourceMessage.getMshSegment()));
        targetResources.add(PatientTransform.fromHl7v2(sourceMessage));

        if (sourceMessage.hasPv1Segment())
            targetResources.add(PatientVisitTransform.fromHl7v2(sourceMessage.getPv1Segment()));

        for (Practitioner practitioner : PractitionerTransform.fromHl7v2(sourceMessage))
            targetResources.add(practitioner);

        if (sourceMessage.hasPv1Segment()) {
            List<Location> locations = LocationConverter.convert(sourceMessage.getPv1Segment().getAssignedPatientLocation());
            for (Location loc : locations)
                targetResources.add(loc);
        }
        return createBundle(targetResources);
    }

    private static Bundle createBundle(List<Resource> resources) {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
