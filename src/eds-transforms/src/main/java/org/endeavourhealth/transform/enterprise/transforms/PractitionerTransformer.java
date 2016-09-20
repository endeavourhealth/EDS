package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class PractitionerTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.Practitioner model = new org.endeavourhealth.transform.enterprise.schema.Practitioner();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            if (fhir.hasName()) {
                HumanName name = fhir.getName();
                model.setName(name.getText());
            }

            for (Practitioner.PractitionerPractitionerRoleComponent role : fhir.getPractitionerRole()) {

                Reference organisationReference = role.getManagingOrganization();

                UUID enterpriseOrgId = findEnterpriseUuid(organisationReference);
                if (enterpriseOrgId != null) {
                    continue;
                }

                model.setOrganisationId(enterpriseOrgId.toString());

                CodeableConcept cc = role.getRole();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES)) {
                        model.setRoleCode(coding.getCode());
                        model.setRoleDesc(coding.getDisplay());
                    }
                }
            }
        }

        data.getPractitioner().add(model);
    }
}
