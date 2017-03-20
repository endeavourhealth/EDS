package org.endeavourhealth.transform.fhirhl7v2;

import org.endeavourhealth.core.xml.transformError.TransformError;

import java.util.List;
import java.util.UUID;

public class FhirHl7v2Filer {
    public void file(UUID exchangeId, String exchangeBody, UUID serviceId, UUID systemId,
                     TransformError transformError, List<UUID> batchIds, TransformError previousErrors) throws Exception {

        System.out.println(exchangeBody);
    }
}
