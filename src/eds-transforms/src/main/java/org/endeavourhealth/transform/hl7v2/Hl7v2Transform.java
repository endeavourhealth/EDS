package org.endeavourhealth.transform.hl7v2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.pretransform.HomertonPreTransform;
import org.endeavourhealth.transform.hl7v2.transform.AdtMessageTransform;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Resource;

public class Hl7v2Transform {
    public static String transform(String message) throws Exception {
        AdtMessage sourceMessage = new AdtMessage(message);

        if (!sourceMessage.hasMshSegment())
            throw new TransformException("MSH segment not found");

        if (sourceMessage.getMshSegment().getSendingFacility().equals("HOMERTON"))
            HomertonPreTransform.preTransform(sourceMessage);

        Bundle targetBundle = AdtMessageTransform.transform(sourceMessage);

        return getPrettyJson(targetBundle);
    }

    private static String getPrettyJson(Resource resource) throws Exception {
        String json = new JsonParser().composeString(resource);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        com.google.gson.JsonParser jp = new com.google.gson.JsonParser();
        JsonElement je = jp.parse(json);
        return gson.toJson(je);
    }
}
