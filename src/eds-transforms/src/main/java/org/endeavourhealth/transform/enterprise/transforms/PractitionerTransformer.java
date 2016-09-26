package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class PractitionerTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Practitioner model = new org.endeavourhealth.core.xml.enterprise.Practitioner();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            if (fhir.hasName()) {
                HumanName name = fhir.getName();
                model.setName(name.getText());
            }

            for (Practitioner.PractitionerPractitionerRoleComponent role : fhir.getPractitionerRole()) {

                Reference organisationReference = role.getManagingOrganization();

                UUID enterpriseOrgId = findEnterpriseUuid(organisationReference);
                if (enterpriseOrgId == null) {
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

        //the EMIS test data has practitioners that point to non-exist organisations,
        //so, in order to file them in enterprise, we sub in the main org ID
        if (model.getOrganisationId() == null) {
            model.setOrganisationId(enterpriseOrganisationUuid.toString());
        }

        data.getPractitioner().add(model);
    }
}
