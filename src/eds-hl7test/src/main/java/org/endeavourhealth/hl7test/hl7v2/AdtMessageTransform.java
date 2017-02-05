package org.endeavourhealth.hl7test.hl7v2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.endeavourhealth.hl7test.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.hl7test.hl7v2.transform.MessageHeaderTransform;
import org.endeavourhealth.hl7test.hl7v2.transform.PatientTransform;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class AdtMessageTransform {
    public static String transform(String adtMessage) throws Exception {

        AdtMessage sourceMessage = new AdtMessage(adtMessage);

        List<Resource> targetResources = new ArrayList<>();

        targetResources.add(MessageHeaderTransform.fromHl7v2(sourceMessage.getMshSegment()));
        targetResources.add(PatientTransform.fromHl7v2(sourceMessage));


        Bundle targetBundle = createBundle(targetResources);
        return getPrettyJson(targetBundle);
    }

    private static Bundle createBundle(List<Resource> resources) {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }

    private static String getPrettyJson(Resource resource) throws Exception {
        String json = new JsonParser().composeString(resource);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        com.google.gson.JsonParser jp = new com.google.gson.JsonParser();
        JsonElement je = jp.parse(json);
        return gson.toJson(je);
    }
}
