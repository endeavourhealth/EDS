package org.endeavourhealth.transform.tpp.transforms;

import org.endeavourhealth.transform.tpp.schema.*;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class PatientTransformer {

    public static void transform(String patientUid, Patient tppPatient, List<Resource> resources) {

        Identity id = tppPatient.getIdentity();
        Demographics demographics = tppPatient.getDemographics();
        DemographicTransformer.transform(patientUid, id, demographics, resources);

        Clinical clinical = tppPatient.getClinical();
        ClinicalTransformer.transform(clinical, resources);

        NonClinical nonClinical = tppPatient.getNonClinical();
        NonClinicalTransformer.transform(nonClinical, resources);
    }
}
