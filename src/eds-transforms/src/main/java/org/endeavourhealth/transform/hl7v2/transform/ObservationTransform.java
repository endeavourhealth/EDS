package org.endeavourhealth.transform.hl7v2.transform;


import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.segments.ObxSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.CodeableConceptHelper;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.StringType;
import org.hl7.fhir.instance.model.Type;

public class ObservationTransform {

    public static Observation fromHl7v2(ObxSegment source) throws ParseException, TransformException {
        Observation observation = new Observation();

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.setCode(CodeableConceptHelper.getCodeableConceptFromString(source.getObservationIdentifier().getAsString()));

        observation.setValue(CodeableConceptHelper.getCodeableConceptFromString(source.getObservationValue()));

        return observation;
    }

}
