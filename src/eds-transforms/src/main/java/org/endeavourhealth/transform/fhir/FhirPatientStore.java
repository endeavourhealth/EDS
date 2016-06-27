package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class FhirPatientStore {

    private String patientGuid = null;
    private String organisationGuid = null;
    private String organisationOds = null;
    private List<Resource> resourcesToSave = new ArrayList<>();
    private List<Resource> resourcesToDelete = new ArrayList<>();

    public FhirPatientStore(String patientGuid, String organisationGuid, String organisationOds) {
        this.patientGuid = patientGuid;
        this.organisationGuid = organisationGuid;
        this.organisationOds = organisationOds;
    }

    public void addResourceToSave(Resource r) {
        resourcesToSave.add(r);
    }
    public void addResourceToDelete(Resource r) {
        resourcesToDelete.add(r);
    }

    public String getPatientGuid() {
        return patientGuid;
    }

    public String getOrganisationGuid() {
        return organisationGuid;
    }

    public String getOrganisationOds() {
        return organisationOds;
    }

    public List<Resource> getResourcesToSave() {
        return resourcesToSave;
    }

    public List<Resource> getResourcesToDelete() {
        return resourcesToDelete;
    }

    /**
     * commits this store to the database, performing inserts/updated/delees as required
     */
    public void save() {

        //PersonResource
    }
}
