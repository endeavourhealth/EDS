package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.Organisation;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class OrganisationTransformer extends AbstractTransformer {

    private String orgOdsCode = null;

    public OrganisationTransformer(String orgOdsCode) {
        this.orgOdsCode = orgOdsCode;
    }

    public void transform(ResourceByExchangeBatch resource,
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
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Organization fhir = (Organization)deserialiseResouce(resource);

            if (fhir.hasIdentifier()) {
                String odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
                model.setOdsCode(odsCode);
            }

            //if the organisation ODS code matches the one we're filing data for, then use the enterpriseOrgUuid
            //that we've already generated for the organisation that all the other tables point to
            if (model.getSaveMode() == SaveMode.INSERT) {

                if (model.getOdsCode() != null
                        && model.getOdsCode().equalsIgnoreCase(orgOdsCode)) {
                    model.setId(enterpriseOrganisationUuid.toString());

                    //make sure the mapping table permanently links this org to the enterprise ID
                    setEnterpriseUuid(fhir.getResourceType().toString(), UUID.fromString(fhir.getId()), enterpriseOrganisationUuid);
                }
            }

            model.setName(fhir.getName());

            if (fhir.hasPartOf()) {

                //the EMIS test pack has organisations that refer to parent orgs that don't exist, so
                //this is wrapped in a try/catch to handle that failure to find the parent
                try {
                    Reference partOfReference = fhir.getPartOf();
                    Organization partOfOrganisation = (Organization)findResource(partOfReference, otherResources);

                    if (partOfOrganisation != null) {
                        UUID partOfOrganisationUuid = findEnterpriseUuid(partOfOrganisation);
                        if (partOfOrganisationUuid != null) {
                            model.setParentOrganisationId(partOfOrganisationUuid.toString());
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

        data.getOrganisation().add(model);
    }
}
