package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.LocationType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.TypeOfLocationType;
import org.endeavourhealth.transform.fhir.ContactPointHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LocationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationTransformer.class);

    public static void transform(MedicalRecordType medicalRecordType, List<Resource> resources) throws TransformException
    {
        List<TypeOfLocationType> locationTypes = medicalRecordType.getLocationTypeList().getLocationType();

        for (LocationType locationType : medicalRecordType.getLocationList().getLocation()) {
            resources.add(createLocation(locationType));
        }
    }

    private static Location createLocation(LocationType locationType) throws TransformException
    {
        Location location = new Location();
        location.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));

        location.setId(locationType.getGUID());

        location.setName(locationType.getLocationName());

        if (locationType.getAddress() != null)
            location.setAddress(org.endeavourhealth.transform.emis.emisopen.transforms.common.AddressConverter.convert(locationType.getAddress(), Address.AddressUse.WORK));

        List<ContactPoint> contactPoints = ContactPointHelper.createWorkContactPoints(locationType.getTelephone1(), locationType.getTelephone2(), locationType.getFax(), locationType.getEmail());

        for (ContactPoint contactPoint : contactPoints)
            location.addTelecom(contactPoint);

        location.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, locationType.getGUID()));

        return location;
    }

}
