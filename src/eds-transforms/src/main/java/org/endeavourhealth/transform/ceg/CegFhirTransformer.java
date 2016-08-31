package org.endeavourhealth.transform.ceg;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;

import java.util.*;

public class CegFhirTransformer {

    public static void transformFromFhir(UUID batchId, List<UUID> resourceIds) throws Exception {

        List<Resource> resources = retrieveResources(batchId, resourceIds);



      /*  d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Slot
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Schedule
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,ReferralRequest
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,ProcedureRequest
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Procedure
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Practitioner
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Patient
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Organization
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Observation
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,MedicationStatement
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,MedicationOrder
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Location
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Immunization
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,FamilyMemberHistory
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,EpisodeOfCare
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Encounter
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Condition
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,Appointment
        d6100b3b-112d-4455-8b1c-bd3a54711916,17bf256d-49a1-403f-83ef-35c74a463fe7,AllergyIntolerance
*/
    }

    private static List<Resource> retrieveResources(UUID batchId, List<UUID> resourceIds) throws Exception {

        //retrieve the resources and strip out any that aren't in the list of resource IDs
        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        Set<UUID> hsResourcesToKeep = new HashSet<>(resourceIds);

        List<Resource> ret = new ArrayList<>();

        for (ResourceByExchangeBatch resourceByExchangeBatch: resourcesByExchangeBatch) {
            UUID resourceId = resourceByExchangeBatch.getResourceId();
            if (hsResourcesToKeep.contains(resourceId)) {

                String json = resourceByExchangeBatch.getResourceData();
                Resource r = new JsonParser().parse(json);
                ret.add(r);
            }
        }

        return ret;
    }
}
