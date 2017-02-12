package org.endeavourhealth.transform.hl7v2.specific;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.specific.homerton.HomertonPreTransform;

public class PreTransform {
    public static AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        Validate.notNull(sourceMessage);

        if (sourceMessage.getMshSegment().getSendingFacility().equals("HOMERTON"))
            sourceMessage = HomertonPreTransform.preTransform(sourceMessage);

        return sourceMessage;
    }
}
