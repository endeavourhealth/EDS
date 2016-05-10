package org.endeavourhealth.core.transform.tpp;

import org.endeavourhealth.core.transform.tpp.schema.*;
import org.endeavourhealth.core.transform.tpp.transforms.ClinicalTransformer;
import org.endeavourhealth.core.transform.tpp.transforms.DemographicTransformer;
import org.endeavourhealth.core.transform.tpp.transforms.NonClinicalTransformer;
import org.endeavourhealth.core.transform.tpp.transforms.PatientTransformer;
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

        Patient tppPatient = tppRecord.getPatient();
        PatientTransformer.transform(tppPatient, fhirResources);

        return fhirResources;
    }
}