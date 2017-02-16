package org.endeavourhealth.transform.tpp;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.transform.tpp.xml.schema.Metadata;
import org.endeavourhealth.transform.tpp.xml.schema.Patient;
import org.endeavourhealth.transform.tpp.xml.schema.PatientRecord;
import org.endeavourhealth.transform.tpp.xml.transforms.MetadataTransformer;
import org.endeavourhealth.transform.tpp.xml.transforms.PatientTransformer;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public final class TppXmlToFhirTransformer {

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