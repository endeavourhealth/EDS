package org.endeavourhealth.transform.vitrucare;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.FhirToXTransformerBase;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FhirToVitruCareXmlTransformer extends FhirToXTransformerBase {

    private static final Logger LOG = LoggerFactory.getLogger(FhirToVitruCareXmlTransformer.class);

    //private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static String transformFromFhir(UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> filteredResources = getResources(batchId, resourceIds);

        //see if there's any patient-data in the resources
        List<ResourceByExchangeBatch> patientResources = new ArrayList<>();

        for (ResourceByExchangeBatch resource: filteredResources) {
            String typeString = resource.getResourceType();
            ResourceType type = ResourceType.valueOf(typeString);
            if (FhirResourceFiler.isPatientResource(type)) {
                patientResources.add(resource);
            }
        }

        //if there's no patient data, juat return null since there's nothing to send on
        if (patientResources.isEmpty()) {
            return null;
        }

        //GUID =

        //byte[] bytes = data.writeToZip();
        //return Base64.getEncoder().encodeToString(bytes);
        return null;
    }
}
