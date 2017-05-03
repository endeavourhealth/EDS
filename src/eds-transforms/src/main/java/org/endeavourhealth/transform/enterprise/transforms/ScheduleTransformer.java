package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class ScheduleTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleTransformer.class);

    public boolean shouldAlwaysTransform() {
        return false;
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName,
                          UUID protocolId) throws Exception {

        Schedule fhir = (Schedule)resource;

        long id;
        long organisationId;
        Long practitionerId = null;
        Date startDate = null;
        String type = null;
        String location = null;

        id = enterpriseId.longValue();
        organisationId = enterpriseOrganisationId.longValue();

        if (fhir.hasActor()) {
            Reference practitionerReference = fhir.getActor();
            practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            if (practitionerId == null) {
                practitionerId = transformOnDemand(practitionerReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
            }
        }

        if (fhir.hasPlanningHorizon()) {
            Period period = fhir.getPlanningHorizon();
            startDate = period.getStart();
        }

        if (fhir.hasExtension()) {
            for (Extension extension: fhir.getExtension()) {
                if (extension.getUrl().equals(FhirExtensionUri.SCHEDULE_LOCATION)) {
                    Reference locationReference = (Reference)extension.getValue();

                    Location fhirLocation = (Location)findResource(locationReference, otherResources);
                    if (fhirLocation != null) {
                        location = fhirLocation.getName();
                    }
                }
            }
        }

        if (fhir.hasType()) {

            //all known and expected data has just a single type, but add this check just in case
            if (fhir.getType().size() > 1) {
                throw new TransformException("Enterprise ScheduleTransformer doesn't support Schedules with multiple types");
            }

            for (CodeableConcept typeCodeableConcept: fhir.getType()) {
                type = typeCodeableConcept.getText();
            }
        }

        org.endeavourhealth.transform.enterprise.outputModels.Schedule model = (org.endeavourhealth.transform.enterprise.outputModels.Schedule)csvWriter;
        model.writeUpsert(id,
            organisationId,
            practitionerId,
            startDate,
            type,
            location);
    }
}
