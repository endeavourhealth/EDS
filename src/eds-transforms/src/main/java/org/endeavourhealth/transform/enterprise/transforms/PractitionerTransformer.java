package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PractitionerTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Practitioner model = new org.endeavourhealth.core.xml.enterprise.Practitioner();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            if (fhir.hasName()) {
                HumanName name = fhir.getName();
                model.setName(name.getText());
            }

            for (Practitioner.PractitionerPractitionerRoleComponent role : fhir.getPractitionerRole()) {

                CodeableConcept cc = role.getRole();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES)) {
                        model.setRoleCode(coding.getCode());
                        model.setRoleDesc(coding.getDisplay());
                    }
                }

                Reference organisationReference = role.getManagingOrganization();
                Integer enterpriseOrgId = findEnterpriseId(organisationReference);

                if (enterpriseOrgId == null) {
                    LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " refers to " + organisationReference.getReference() + " that doesn't exist");
                } else {
                    model.setOrganizationId(enterpriseOrgId);
                }
            }
        }

        //the EMIS test data has practitioners that point to non-exist organisations,
        //so, in order to file them in enterprise, we sub in the main org ID
        if (model.getOrganizationId() == 0) {
            model.setOrganizationId(enterpriseOrganisationUuid);
        }

        data.getPractitioner().add(model);
    }
}
