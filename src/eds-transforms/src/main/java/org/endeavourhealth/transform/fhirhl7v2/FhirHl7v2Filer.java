package org.endeavourhealth.transform.fhirhl7v2;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FhirHl7v2Filer {
    public void file(UUID exchangeId, String exchangeBody, UUID serviceId, UUID systemId,
                     TransformError transformError, List<UUID> batchIds, TransformError previousErrors) throws Exception {

        final int maxFilingThreads = 1;
        FhirResourceFiler fhirResourceFiler = new FhirResourceFiler(exchangeId, serviceId, systemId, transformError, batchIds, maxFilingThreads);

        Resource bundleResource = FhirResourceHelper.deserialiseResouce(exchangeBody);

        if (bundleResource.getResourceType() != ResourceType.Bundle)
            throw new Exception("Resource is not a bundle");

        Bundle bundle = (Bundle)bundleResource;

        saveAdminResources(fhirResourceFiler, bundle);
        savePatientResources(fhirResourceFiler, bundle);

        fhirResourceFiler.waitToFinish();
    }

    private void saveAdminResources(FhirResourceFiler fhirResourceFiler, Bundle bundle) throws Exception {
        List<Resource> adminResources = bundle
                .getEntry()
                .stream()
                .map(t -> t.getResource())
                .filter(t -> !FhirResourceFiler.isPatientResource(t))
                .filter(t -> t.getResourceType() != ResourceType.MessageHeader)
                .collect(Collectors.toList());

        fhirResourceFiler.saveAdminResource(null, false, adminResources.toArray(new Resource[0]));
    }

    private void savePatientResources(FhirResourceFiler fhirResourceFiler, Bundle bundle) throws Exception {
        List<Resource> patientResources = bundle
                .getEntry()
                .stream()
                .map(t -> t.getResource())
                .filter(t -> FhirResourceFiler.isPatientResource(t))
                .collect(Collectors.toList());

        Patient patient = patientResources
                .stream()
                .filter(t -> t.getResourceType() == ResourceType.Patient)
                .map(t -> (Patient)t)
                .collect(StreamExtension.singleCollector());

        fhirResourceFiler.savePatientResource(null, false, patient.getId(), patientResources.toArray(new Resource[0]));
    }
}
