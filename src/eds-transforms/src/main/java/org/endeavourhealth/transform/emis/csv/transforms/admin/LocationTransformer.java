package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Location;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class LocationTransformer {

    public static HashMap<UUID, Location> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        //to create the FHIR location resources, parse the Admin_OrganisationLocation file first
        //to get a map of which organisations each location belongs to
        HashMap<UUID, UUID> hmLocationToOrganisation = OrganisationLocationTransformer.transform(folderPath, csvFormat);

        HashMap<UUID, Location> fhirLocationMap = new HashMap<>();

        Admin_Location parser = new Admin_Location(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createLocation(parser, fhirLocationMap, hmLocationToOrganisation);
            }
        } finally {
            parser.close();
        }

        return fhirLocationMap;
    }

    private static void createLocation(Admin_Location locationParser, HashMap<UUID, Location> fhirLocations, HashMap<UUID, UUID> hmLocationToOrganisation) throws Exception {

        Location fhirLocation = new Location();
        fhirLocation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));

        UUID locationGuid = locationParser.getLocationGuid();
        fhirLocation.setId(locationGuid.toString());

        //add to map for later use
        fhirLocations.put(locationGuid, fhirLocation);

        String houseNameFlat = locationParser.getHouseNameFlatNumber();
        String numberAndStreet = locationParser.getNumberAndStreet();
        String village = locationParser.getVillage();
        String town = locationParser.getTown();
        String county = locationParser.getCounty();
        String postcode = locationParser.getPostcode();

        Address fhirAddress = AddressConverter.createAddress(Address.AddressUse.WORK, houseNameFlat, numberAndStreet, village, town, county, postcode);
        fhirLocation.setAddress(fhirAddress);

        String phoneNumber = locationParser.getPhoneNumber();
        if (!Strings.isNullOrEmpty(phoneNumber)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, phoneNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String faxNumber = locationParser.getFaxNumber();
        if (!Strings.isNullOrEmpty(faxNumber)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.FAX, ContactPoint.ContactPointUse.WORK, faxNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String email = locationParser.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.WORK, email);
            fhirLocation.addTelecom(fhirContact);
        }

        Date openDate = locationParser.getOpenDate();
        Date closeDate = locationParser.getCloseDate();
        boolean deleted = locationParser.getDeleted();
        Period fhirPeriod = PeriodHelper.createPeriod(openDate, closeDate);
        if (PeriodHelper.isActive(fhirPeriod) && !deleted) {
            fhirLocation.setStatus(Location.LocationStatus.ACTIVE);
        } else {
            fhirLocation.setStatus(Location.LocationStatus.INACTIVE);
        }
        fhirLocation.addExtension(ExtensionHelper.createExtension(FhirExtensionUri.LOCATION_ACTIVE_PERIOD, fhirPeriod));

        String name = locationParser.getLocationName();
        fhirLocation.setName(name);

        String type = locationParser.getLocationTypeDescription();
        fhirLocation.setType(CodeableConceptHelper.createCodeableConcept(type));

        UUID parentLocationGuid = locationParser.getParentLocationId();
        if (parentLocationGuid != null) {
            fhirLocation.setPartOf(ReferenceHelper.createReference(ResourceType.Location, parentLocationGuid.toString()));
        }

        UUID organisationGuid = hmLocationToOrganisation.get(locationGuid);
        if (organisationGuid != null) {
            fhirLocation.setPartOf(ReferenceHelper.createReference(ResourceType.Organization, organisationGuid.toString()));
        }

        //TODO - do we need somewhere to store location MainContactName in FHIR

    }
}
