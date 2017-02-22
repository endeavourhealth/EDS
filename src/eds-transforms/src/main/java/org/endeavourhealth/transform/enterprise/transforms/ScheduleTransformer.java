package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class ScheduleTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Schedule model = data.getSchedules();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {
            Schedule fhir = (Schedule)deserialiseResouce(resource);

            int id;
            int organisationId;
            Integer practitionerId = null;
            Date startDate = null;
            String type = null;
            String location = null;

            id = enterpriseId.intValue();
            organisationId = enterpriseOrganisationUuid.intValue();

            if (fhir.hasActor()) {
                Reference actorReference = fhir.getActor();
                practitionerId = findEnterpriseId(data.getPractitioners(), actorReference);
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

            model.writeUpsert(id,
                organisationId,
                practitionerId,
                startDate,
                type,
                location);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Schedule model = new org.endeavourhealth.core.xml.enterprise.Schedule();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Schedule fhir = (Schedule)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            if (fhir.hasActor()) {
                Reference actorReference = fhir.getActor();
                Integer enterpriseActorUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Practitioner(), actorReference);
                if (enterpriseActorUuid != null) {
                    model.setPractitionerId(enterpriseActorUuid);
                }
            }

            if (fhir.hasPlanningHorizon()) {
                Period period = fhir.getPlanningHorizon();
                model.setStartDate(convertDate(period.getStart()));
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.SCHEDULE_LOCATION)) {
                        Reference locationReference = (Reference)extension.getValue();

                        Location location = (Location)findResource(locationReference, otherResources);
                        if (location != null) {
                            model.setLocation(location.getName());
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
                    String type = typeCodeableConcept.getText();
                    model.setType(type);
                }
            }
        }

        data.getSchedule().add(model);
    }*/
}
