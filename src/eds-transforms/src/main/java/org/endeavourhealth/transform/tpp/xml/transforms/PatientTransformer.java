package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.tpp.xml.schema.*;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class PatientTransformer {

    public static void transform(String patientUid, Patient tppPatient, List<Resource> resources)  throws TransformException {

        Identity id = tppPatient.getIdentity();
        Demographics demographics = tppPatient.getDemographics();
        DemographicTransformer.transform(patientUid, id, demographics, resources);

        Clinical clinical = tppPatient.getClinical();
        ClinicalTransformer.transform(clinical, resources);

        NonClinical nonClinical = tppPatient.getNonClinical();
        NonClinicalTransformer.transform(nonClinical, resources);
    }
}
