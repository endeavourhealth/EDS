package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.*;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class PatientTransformer {

    public static void transform(Patient tppPatient, List<Resource> resources) {
        Identity id = tppPatient.getIdentity();
        Demographics demographics = tppPatient.getDemographics();
        DemographicTransformer.transform(id, demographics, resources);

        Clinical clinical = tppPatient.getClinical();
        ClinicalTransformer.transform(clinical, resources);

        NonClinical nonClinical = tppPatient.getNonClinical();
        NonClinicalTransformer.transform(nonClinical, resources);
    }
}
