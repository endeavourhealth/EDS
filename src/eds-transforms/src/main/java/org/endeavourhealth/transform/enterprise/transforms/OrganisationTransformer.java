package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.Organization;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;

public class OrganisationTransformer extends AbstractTransformer {

    private String orgOdsCode = null;

    public OrganisationTransformer(String orgOdsCode) {
        this.orgOdsCode = orgOdsCode;
    }

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        Organization model = new Organization();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            org.hl7.fhir.instance.model.Organization fhir = (org.hl7.fhir.instance.model.Organization)deserialiseResouce(resource);

            if (fhir.hasIdentifier()) {
                String odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
                model.setOdsCode(odsCode);
            }

            //if the organisation ODS code matches the one we're filing data for, then save the Enterprise ID
            //we've generated for the org in the the enterprise_organisation_id mapping table
            if (model.getSaveMode() == SaveMode.INSERT
                && model.getOdsCode() != null
                && model.getOdsCode().equalsIgnoreCase(orgOdsCode)) {

                new EnterpriseIdMapRepository().saveEnterpriseOrganisationIdMapping(orgOdsCode, new Integer(model.getId()));
            }

            model.setName(fhir.getName());

            if (fhir.hasPartOf()) {

                //the EMIS test pack has organisations that refer to parent orgs that don't exist, so
                //this is wrapped in a try/catch to handle that failure to find the parent
                try {
                    Reference partOfReference = fhir.getPartOf();
                    org.hl7.fhir.instance.model.Organization partOfOrganisation = (org.hl7.fhir.instance.model.Organization)findResource(partOfReference, otherResources);

                    if (partOfOrganisation != null) {
                        Integer partOfOrganisationUuid = findEnterpriseId(partOfOrganisation);
                        if (partOfOrganisationUuid != null) {
                            model.setParentOrganizationId(partOfOrganisationUuid);
                        }
                    }

                } catch (Exception ex) {
                    //ignore any failure
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

        data.getOrganization().add(model);
    }
}
