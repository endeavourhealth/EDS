package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.core.data.ehr.PersonResourceRepository;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FhirPatientStore {

    private static PersonResourceRepository personResourceRepository = new PersonResourceRepository();

    private String organisationOds = null;
    private UUID personId = null;
    private List<Resource> resourcesToSave = new ArrayList<>();
    private List<Resource> resourcesToDelete = new ArrayList<>();

    public FhirPatientStore() {}

    public void addResourceToSave(Resource r) {
        resourcesToSave.add(r);
    }
    public void addResourceToDelete(Resource r) {
        resourcesToDelete.add(r);
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public String getOrganisationOds() {
        return organisationOds;
    }

    public void setOrganisationOds(String organisationOds) {
        this.organisationOds = organisationOds;
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
    public void save(UUID serviceId, UUID systemInstanceId) throws Exception {

        JsonParser parser = new JsonParser();

        List<PersonResource> toSave = new ArrayList<>();
        for (Resource resource: resourcesToSave) {

            PersonResource personResource = new PersonResource();
            personResource.setPersonId(personId);
            personResource.setResourceType(resource.getResourceType().toString());
            personResource.setServiceId(serviceId);
            personResource.setSystemInstanceId(systemInstanceId);
            personResource.setResourceId(resource.getId());
            //personResource.setEffectiveDate(); //TODO - set effective date on PatientResource
            //personResource.setVersion(); //TODO - set version on PatientResource
            personResource.setLastUpdated(new Date());
            //personResource.setResourceMetadata(); //TODO - set metadata date on PatientResource
            //personResource.setSchemaVersion(); //TODO - set schema version on PatientResource
            personResource.setResourceData(parser.composeString(resource));

            toSave.add(personResource);
        }

        List<PersonResource> toDelete = new ArrayList<>();
        for (Resource resource: resourcesToDelete) {

            PersonResource personResource = new PersonResource();
            personResource.setPersonId(personId);
            personResource.setResourceType(resource.getResourceType().toString());
            personResource.setServiceId(serviceId);
            personResource.setSystemInstanceId(systemInstanceId);
            personResource.setResourceId(resource.getId());
            //personResource.setEffectiveDate(); //TODO - set effective date on PatientResource
            //personResource.setVersion(); //TODO - set version on PatientResource
            personResource.setLastUpdated(new Date());
            //personResource.setResourceMetadata(); //TODO - set metadata date on PatientResource
            //personResource.setSchemaVersion(); //TODO - set schema version on PatientResource
            personResource.setResourceData(parser.composeString(resource));

            toDelete.add(personResource);
        }

        personResourceRepository.insert(toSave);
        personResourceRepository.delete(toDelete);

    }
}
