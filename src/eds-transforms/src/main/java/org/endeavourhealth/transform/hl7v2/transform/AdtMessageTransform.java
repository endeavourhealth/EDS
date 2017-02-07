package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class AdtMessageTransform {
    public static Bundle transform(AdtMessage sourceMessage) throws Exception {
        Validate.notNull(sourceMessage);

        List<Resource> targetResources = new ArrayList<>();

        targetResources.add(MessageHeaderTransform.fromHl7v2(sourceMessage.getMshSegment()));
        targetResources.add(PatientTransform.fromHl7v2(sourceMessage));

        for (Practitioner practitioner : PractitionerTransform.fromHl7v2(sourceMessage))
            targetResources.add(practitioner);

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
