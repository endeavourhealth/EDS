package org.endeavourhealth.core.fhirStorage;

import java.util.UUID;

public abstract class FhirDeletionService {

    public static void deleteAllData(UUID serviceId) {

        //need to delete from:
        //resource_by_exchange_batch  - batch_id, resource_type, resource_id
        //resource_by_service = service_id, system_id, resource_type, resource_id
        //resource_history - resource_id, resource_type
        //resource_history_by_service - service_id, system_id, resource_type, created_at????, resource_id????

        //1. find all exchanges for the service
        //2. use Exchange_Batch to get all batch IDs for exchanges
        //3. use resource_by_exchange_batch to get all resource types and IDs


    }
}
