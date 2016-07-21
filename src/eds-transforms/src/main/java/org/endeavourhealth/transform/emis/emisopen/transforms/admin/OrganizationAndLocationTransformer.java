package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.LocationType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.TypeOfLocationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.AddressConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.endeavourhealth.transform.fhir.ContactPointCreater.createWorkContactPoints;

public class OrganizationAndLocationTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> organizationAndLocations = new ArrayList<>();

        for (LocationType locationType : medicalRecordType.getLocationList().getLocation())
        {
            organizationAndLocations.add(createOrganization(locationType, getTypeOfLocationType(locationType, medicalRecordType.getLocationTypeList().getLocationType())));
            organizationAndLocations.add(createLocation(locationType));
        }

        return organizationAndLocations;
    }

    private static Organization createOrganization(LocationType locationType, TypeOfLocationType typeOfLocationType) throws TransformException
    {
        Organization organization = new Organization();

        organization.setId(locationType.getGUID());
        organization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        organization.setName(locationType.getLocationName());

        if (!StringUtils.isBlank(locationType.getNationalCode()))
            organization.addIdentifier(new Identifier().setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE).setValue(locationType.getNationalCode()));

        if (typeOfLocationType != null)
            if (StringUtils.isNotBlank(typeOfLocationType.getDescription()))
                organization.setType(new CodeableConcept().setText(typeOfLocationType.getDescription()));

        organization.addExtension(new Extension().setUrl(FhirExtensionUri.MAIN_LOCATION).setValue(ReferenceHelper.createReference(ResourceType.Location, locationType.getGUID())));

        return organization;
    }

    private static Location createLocation(LocationType locationType) throws TransformException
    {
        Location location = new Location();

        location.setId(locationType.getGUID());
        location.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));

        location.setName(locationType.getLocationName());

        if (locationType.getAddress() != null)
            location.setAddress(AddressConverter.convert(locationType.getAddress(), Address.AddressUse.WORK));

        List<ContactPoint> contactPoints = createWorkContactPoints(locationType.getTelephone1(), locationType.getTelephone2(), locationType.getFax(), locationType.getEmail());

        for (ContactPoint contactPoint : contactPoints)
            location.addTelecom(contactPoint);

        location.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, locationType.getGUID()));

        return location;
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
