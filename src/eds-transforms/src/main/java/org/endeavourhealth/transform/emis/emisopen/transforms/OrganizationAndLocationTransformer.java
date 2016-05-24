package org.endeavourhealth.transform.emis.emisopen.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.LocationType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.TypeOfLocationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.converters.AddressConverter;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

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
        organization.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ORGANIZATION));

        organization.setName(locationType.getLocationName());

        if (!StringUtils.isBlank(locationType.getNationalCode()))
            organization.addIdentifier(new Identifier().setSystem(FhirUris.IDENTIFIER_SYSTEM_ODS_CODE).setValue(locationType.getNationalCode()));

        if (typeOfLocationType != null)
            if (StringUtils.isNotBlank(typeOfLocationType.getDescription()))
                organization.setType(new CodeableConcept().setText(typeOfLocationType.getDescription()));

        organization.addExtension(new Extension().setUrl(FhirUris.EXTENSION_URI_MAINLOCATION).setValue(ReferenceHelper.createReference(ResourceType.Location, locationType.getGUID())));

        return organization;
    }

    private static Location createLocation(LocationType locationType) throws TransformException
    {
        Location location = new Location();

        location.setId(locationType.getGUID());
        location.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_LOCATION));

        location.setName(locationType.getLocationName());

        if (locationType.getAddress() != null)
            location.setAddress(AddressConverter.convert(locationType.getAddress(), Address.AddressUse.WORK));

        for (ContactPoint contactPoint : createContactPoints(locationType))
            location.addTelecom(contactPoint);

        location.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, locationType.getGUID()));

        return location;
    }

    private static List<ContactPoint> createContactPoints(LocationType locationType)
    {
        List<ContactPoint> contactPoints = new ArrayList<>();

        if (StringUtils.isNotBlank(locationType.getTelephone1()))
            contactPoints.add(createWorkContactPoint(locationType.getTelephone1(), ContactPoint.ContactPointSystem.PHONE));

        if (StringUtils.isNotBlank(locationType.getTelephone2()))
            contactPoints.add(createWorkContactPoint(locationType.getTelephone2(), ContactPoint.ContactPointSystem.PHONE));

        if (StringUtils.isNotBlank(locationType.getEmail()))
            contactPoints.add(createWorkContactPoint(locationType.getEmail(), ContactPoint.ContactPointSystem.EMAIL));

        if (StringUtils.isNotBlank(locationType.getFax()))
            contactPoints.add(createWorkContactPoint(locationType.getFax(), ContactPoint.ContactPointSystem.FAX));

        return contactPoints;
    }

    private static ContactPoint createWorkContactPoint(String value, ContactPoint.ContactPointSystem contactPointSystem)
    {
        return new ContactPoint()
                .setSystem(contactPointSystem)
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setValue(value);
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
