package org.endeavourhealth.core;

import org.endeavourhealth.core.data.CassandraConnector;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main (String[] args) {
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible("eds.apiEngineConfiguration");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
/*
            UUID personId = UUID.fromString("b1bd9f09-cb3e-4e13-a660-85bc19b10db0");
            UUID serviceId = UUID.fromString("2a9ef1ff-c6e5-4780-9e84-3be7f52dfc6a");
            UUID systemInstanceId = UUID.fromString("dbe95ca1-e0d6-4300-a7cd-5ef4e423dd8d");
            String resourceId = UUID.randomUUID().toString();
            String version = UUID.randomUUID().toString();
            String resourceType = "Patient";
            Date effectiveDate = new Date();

            PersonResource resource = new PersonResource();
            resource.setPersonId(personId);
            resource.setResourceType(resourceType);
            resource.setServiceId(serviceId);
            resource.setSystemInstanceId(systemInstanceId);
            resource.setResourceId(resourceId);
            resource.setEffectiveDate(effectiveDate);
            resource.setVersion(version);
            resource.setLastUpdated(new Date());
            resource.setResourceMetadata("<metadata></metadata>");
            resource.setSchemaVersion("1.0");
            resource.setResourceData("<patient></patient>");

            PersonResourceRepository repo = new PersonResourceRepository();
            repo.insert(resource);

            String newVersion = UUID.randomUUID().toString();
            resource.setVersion(newVersion);
            repo.update(resource);

            PersonResource resourceFromDb = repo.getByKey(personId, resourceType, serviceId, systemInstanceId, resourceId);

            Iterable<PersonResource> results = repo.getByResourceType(personId, resourceType);

            PersonResourceMetadataRepository metaRepo = new PersonResourceMetadataRepository();
            PersonResourceMetadata metadataResult = metaRepo.getByKey(personId, resourceType, effectiveDate, serviceId, systemInstanceId, resourceId);
            Iterable<PersonResourceMetadata> metadataResults = metaRepo.getByResourceType(personId, resourceType);

            resourceFromDb.setVersion("2");
*/

            UUID serviceId = UUID.fromString("2a9ef1ff-c6e5-4780-9e84-3be7f52dfc6a");
            UUID systemId = UUID.fromString("dbe95ca1-e0d6-4300-a7cd-5ef4e423dd8d");

            Patient patient = new Patient();
            patient.setId("b1bd9f09-cb3e-4e13-a660-85bc19b10db0");

            AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
            allergyIntolerance.setId("cec6b6c5-14f9-4da5-9c53-a61fb80527d1");
            allergyIntolerance.setPatient(new Reference().setReference("Patient/b1bd9f09-cb3e-4e13-a660-85bc19b10db0"));

            Medication medication = new Medication();
            medication.setId("660122da-8a8f-4777-ab73-f267552fa5c0");

            Appointment appointment = new Appointment();
            appointment.setId("e543c43c-41bd-4ebe-af4c-4b2c84443148");
            appointment.getParticipant().add(new Appointment.AppointmentParticipantComponent().setStatus(Appointment.ParticipationStatus.NEEDSACTION));
            appointment.getParticipant().add(new Appointment.AppointmentParticipantComponent().setActor(new Reference().setReference("Practitioner/5540b156-fac9-4430-a604-9da2597ef6f9")));
//            appointment.getParticipant().add(new Appointment.AppointmentParticipantComponent().setActor(new Reference().setReference("Patient/b1bd9f09-cb3e-4e13-a660-85bc19b10db0")));


            UUID exchangeId = UUID.fromString("e8cd3ecc-75c3-48f7-90ff-4fc52ad87479");
            UUID batchId = UUID.fromString("a3090516-9fb0-494a-a1fc-780a8ea67826");
            List<Resource> resources = new ArrayList<>();
            resources.add(patient);
            resources.add(allergyIntolerance);
            resources.add(medication);
            resources.add(appointment);

            FhirStorageService service = new FhirStorageService(serviceId, systemId);
//            service.update(patient);
            service.exchangeBatchUpdate(exchangeId, batchId, resources);

/*
            String resourceType = "observation";
            String vesrion = "3";

            PerfTest tester = new PerfTest();
//            tester.WriteTest();

            List<String> keys = new ArrayList<>();
            for (PerfTestModel observation : tester.getAll()) {
                keys.add(observation.getResourceId());
            }

            Stopwatch stopwatch = Stopwatch.createUnstarted();
            stopwatch.start();
            for (String key : keys ) {
                tester.getByKey(resourceType, key, vesrion);
            }
            stopwatch.stop(); // optional

            long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            System.out.println("Elapsed Time" + millis);


            stopwatch.reset();
            stopwatch.start();
            tester.getByPerson(UUID.fromString("b1bd9f09-cb3e-4e13-a660-85bc19b10db0"), resourceType);
            stopwatch.stop(); // optional

            millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            System.out.println("Elapsed Time" + millis);
*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CassandraConnector.getInstance().close();
        }
    }
}
