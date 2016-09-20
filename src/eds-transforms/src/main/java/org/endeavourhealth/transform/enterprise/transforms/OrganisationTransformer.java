package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.enterprise.schema.Organisation;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class OrganisationTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        Organisation model = new Organisation();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            Organization fhir = (Organization)deserialiseResouce(resource);

            if (fhir.hasIdentifier()) {
                String odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
                model.setOdsCode(odsCode);
            }

            model.setName(fhir.getName());

            if (fhir.hasPartOf()) {
                Reference partOfReference = fhir.getPartOf();
                Organization partOfOrganisation = (Organization)findResource(partOfReference, otherResources);
                if (partOfOrganisation != null) {
                    UUID partOfOrganisationUuid = findEnterpriseUuid(partOfOrganisation);
                    if (partOfOrganisationUuid != null) {
                        model.setParentOrganisationId(partOfOrganisationUuid.toString());
                    }
                }
            }

            if (fhir.hasType()) {
                CodeableConcept cc = fhir.getType();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_ORGANISATION_TYPE)) {

                        model.setTypeCode(coding.getCode());
                        model.setTypeDesc(coding.getDisplay());

                    }
                }
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.ORGANISATION_MAIN_LOCATION)) {

                        Reference locationReference = (Reference)extension.getValue();

                        Location location = (Location)findResource(locationReference, otherResources);
                        if (location != null
                                && location.hasAddress()) {
                            Address address = location.getAddress();
                            if (address.hasPostalCode()) {
                                model.setPostcode(address.getPostalCode());
                            }
                        }
                    }

                }
            }
        }

        data.getOrganisation().add(model);
    }
}
