package org.endeavourhealth.transform.tpp;

import org.endeavourhealth.transform.tpp.schema.*;
import org.endeavourhealth.transform.tpp.transforms.*;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public final class TppTransformer {

    private static final String XSD = "TppPatientRecord.xsd";

    public List<Resource> toFhirFullRecord(String xml) throws Exception
    {
        PatientRecord tppRecord = XmlSerializer.deserializeFromString(PatientRecord.class, xml, XSD);

        List<Resource> fhirResources = new ArrayList<>();

        //transform the metadata first, as a lot of the clinical entities require it
        Metadata tppMetadata = tppRecord.getMetadata();
        MetadataTransformer.transform(tppMetadata, fhirResources);

        Patient tppPatient = tppRecord.getPatient();
        String patientUid = tppRecord.getPatientUID();
        PatientTransformer.transform(patientUid, tppPatient, fhirResources);

        return fhirResources;
    }
}