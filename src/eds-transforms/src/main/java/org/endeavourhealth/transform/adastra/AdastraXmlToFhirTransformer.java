package org.endeavourhealth.transform.adastra;

import org.endeavourhealth.common.utility.XmlHelper;
import org.endeavourhealth.transform.adastra.schema.AdastraCaseDataExport;
import org.endeavourhealth.transform.adastra.transforms.EpisodeTransformer;
import org.endeavourhealth.transform.adastra.transforms.PatientTransformer;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class AdastraXmlToFhirTransformer {

    public static List<Resource> toFhirFullRecord(String xmlPayload) throws Exception {

        //TODO - use XSD to validate received XML

        AdastraCaseDataExport caseReport = XmlHelper.deserialize(xmlPayload, AdastraCaseDataExport.class);

        List<Resource> ret = new ArrayList<>();

        //TODO - handle case reference
        //TODO - handle care number


        PatientTransformer.transform(caseReport, ret);
        EpisodeTransformer.transform(caseReport, ret);

        return ret;
    }
}
