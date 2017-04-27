package org.endeavourhealth.transform.adastra;

import org.endeavourhealth.common.utility.XmlHelper;
import org.endeavourhealth.transform.adastra.schema.AdastraCaseDataExport;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class AdastraXmlToFhirTransformer {

    public static List<Resource> toFhirFullRecord(String xmlPayload) throws Exception {

        AdastraCaseDataExport caseReport = XmlHelper.deserialize(xmlPayload, AdastraCaseDataExport.class);



        return null;
    }
}
