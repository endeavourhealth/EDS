package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.LocationType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.TypeOfLocationType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.OrganisationType;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrganizationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationTransformer.class);

    public static void transform(MedicalRecordType medicalRecordType, List<Resource> resources) throws TransformException {

        List<TypeOfLocationType> locationTypes = medicalRecordType.getLocationTypeList().getLocationType();

        for (LocationType locationType : medicalRecordType.getLocationList().getLocation()) {
            resources.add(createOrganization(locationType, getTypeOfLocationType(locationType, locationTypes)));
        }
    }

    private static Organization createOrganization(LocationType locationType, TypeOfLocationType typeOfLocationType) throws TransformException
    {
        Organization organization = new Organization();
        organization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        organization.setId(locationType.getGUID());

        organization.setName(locationType.getLocationName());

        if (!StringUtils.isBlank(locationType.getNationalCode())) {
            organization.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE, locationType.getNationalCode()));
        }

        if (typeOfLocationType != null) {
            String typeDesc = typeOfLocationType.getDescription();
            if (!Strings.isNullOrEmpty(typeDesc)) {
                CodeableConcept fhirCodeableConcept = null;
                OrganisationType fhirOrgType = convertOrganisationType(typeDesc);
                if (fhirOrgType != null) {
                    fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(fhirOrgType);
                } else {
                    LOG.info("Unmapped organisation type " + typeDesc);
                    fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(typeDesc);
                }
                organization.setType(fhirCodeableConcept);
            }

        }

        organization.addExtension(new Extension().setUrl(FhirExtensionUri.ORGANISATION_MAIN_LOCATION).setValue(ReferenceHelper.createReference(ResourceType.Location, locationType.getGUID())));

        return organization;
    }

    private static OrganisationType convertOrganisationType(String csvOrganisationType) {
        try {
            return OrganisationType.fromDescription(csvOrganisationType);
        } catch (Exception ex) {

            //the below mappings are based on what was present in the EMIS CSV sample files
            //EMIS has been asked for a complete list, but until this is made available, these
            //are the only known types. There are a number of organisation types, such as "Hospice"
            //or "Community" which don't map to any official NHS organisation type
            if (csvOrganisationType.equalsIgnoreCase("General Practice")
                    || csvOrganisationType.equalsIgnoreCase("General Practice Surgery")
                    || csvOrganisationType.equalsIgnoreCase("Main Surgery")) {
                return OrganisationType.GP_PRACTICE;

            } else if (csvOrganisationType.equalsIgnoreCase("CCG")) {
                return OrganisationType.CCG;

            } else if (csvOrganisationType.equalsIgnoreCase("PCT Site")
                    || csvOrganisationType.equalsIgnoreCase("Primary Care Trust")) {
                return OrganisationType.PCT;

            } else if (csvOrganisationType.equalsIgnoreCase("Hospital")
                    || csvOrganisationType.equalsIgnoreCase("NHS Trust Site")
                    || csvOrganisationType.equalsIgnoreCase("NHS Trust")) {
                return OrganisationType.NHS_TRUST;

            } else {
                return null;
            }
        }

    }


    private static TypeOfLocationType getTypeOfLocationType(LocationType locationType, List<TypeOfLocationType> typeOfLocationTypes)
    {
        if (locationType.getLocationTypeID() == null)
            return null;

        if (!StringUtils.isNotBlank(locationType.getLocationTypeID().getGUID()))
            return null;

        for (TypeOfLocationType typeOfLocationType : typeOfLocationTypes)
            if (typeOfLocationType.getGUID() != null)
                if (typeOfLocationType.getGUID().equalsIgnoreCase(locationType.getLocationTypeID().getGUID()))
                    return typeOfLocationType;

        return null;
    }
}
