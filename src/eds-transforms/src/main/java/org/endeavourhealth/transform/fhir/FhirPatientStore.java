package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class FhirPatientStore {

    private String patientId = null;
    private List<Resource> resourcesToSave = new ArrayList<>();
    private List<Resource> resourcesToDelete = new ArrayList<>();

    public FhirPatientStore(String patientId) {
        this.patientId = patientId;
    }

    public void addResourceToSave(Resource r) {
        resourcesToSave.add(r);
    }
    public void addResourceToDelete(Resource r) {
        resourcesToDelete.add(r);
    }

    public List<Resource> getResourcesToSave() {
        return resourcesToSave;
    }
    public List<Resource> getResourcesToDelete() {
        return resourcesToDelete;
    }
}
