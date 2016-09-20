package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class ScheduleTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.Schedule model = new org.endeavourhealth.transform.enterprise.schema.Schedule();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            Schedule fhir = (Schedule)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            if (fhir.hasActor()) {
                Reference actorReference = fhir.getActor();
                UUID enterpriseActorUuid = findEnterpriseUuid(actorReference);
                if (enterpriseActorUuid != null) {
                    model.setPractitionerId(enterpriseActorUuid.toString());
                }
            }

            if (fhir.hasPlanningHorizon()) {
                Period period = fhir.getPlanningHorizon();
                model.setDate(convertDate(period.getStart()));
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.SCHEDULE_LOCATION)) {
                        StringType location = (StringType)extension.getValue();
                        model.setLocation(location.toString());
                    }
                }
            }

            if (fhir.hasType()) {

                //all known and expected data has just a single type, but add this check just in case
                if (fhir.getType().size() > 1) {
                    throw new TransformException("Enterprise ScheduleTransformer doesn't support Schedules with multiple types");
                }

                for (CodeableConcept typeCodeableConcept: fhir.getType()) {
                    String type = typeCodeableConcept.getText();
                    model.setType(type);
                }
            }
        }

        data.getSchedule().add(model);
    }
}
