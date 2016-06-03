package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Location;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.fhir.AddressConverter;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LocationTransformer {

    public static HashMap<UUID, Location> transformLocations(String folderPath, CSVFormat csvFormat) throws Exception {

        //to create the FHIR location resources, parse the Admin_OrganisationLocation file first
        //to get a map of which organisations each location belongs to
        HashMap<UUID, UUID> hmLocationToOrganisation = OrganisationLocationTransformer.transformOrganisationLocations(folderPath, csvFormat);

        HashMap<UUID, Location> fhirLocationMap = new HashMap<>();

        Admin_Location locationParser = new Admin_Location(folderPath, csvFormat);
        try {
            while (locationParser.nextRecord()) {
                transformLocation(locationParser, fhirLocationMap, hmLocationToOrganisation);
            }
        } finally {
            locationParser.close();
        }

        return fhirLocationMap;
    }

    private static void transformLocation(Admin_Location locationParser, HashMap<UUID, Location> fhirLocations, HashMap<UUID, UUID> hmLocationToOrganisation) throws Exception {

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
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, phoneNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String faxNumber = locationParser.getFaxNumber();
        if (!Strings.isNullOrEmpty(faxNumber)) {
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.FAX, ContactPoint.ContactPointUse.WORK, faxNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String email = locationParser.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint fhirContact = Fhir.createContactPoint(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.WORK, email);
            fhirLocation.addTelecom(fhirContact);
        }

        Date openDate = locationParser.getOpenDate();
        Date closeDate = locationParser.getCloseDate();
        boolean deleted = locationParser.getDeleted();
        Period fhirPeriod = Fhir.createPeriod(openDate, closeDate);
        if (Fhir.isActive(fhirPeriod) && !deleted) {
            fhirLocation.setStatus(Location.LocationStatus.ACTIVE);
        } else {
            fhirLocation.setStatus(Location.LocationStatus.INACTIVE);
        }
        fhirLocation.addExtension(Fhir.createExtension(FhirExtensionUri.LOCATION_ACTIVE_PERIOD, fhirPeriod));

        String name = locationParser.getLocationName();
        fhirLocation.setName(name);

        String type = locationParser.getLocationTypeDescription();
        fhirLocation.setType(Fhir.createCodeableConcept(type));

        UUID parentLocationGuid = locationParser.getParentLocationId();
        if (parentLocationGuid != null) {
            fhirLocation.setPartOf(Fhir.createReference(ResourceType.Location, parentLocationGuid.toString()));
        }

        UUID organisationGuid = hmLocationToOrganisation.get(locationGuid);
        if (organisationGuid != null) {
            fhirLocation.setPartOf(Fhir.createReference(ResourceType.Organization, organisationGuid.toString()));
        }

        //TODO - do we need somewhere to store location MainContactName in FHIR

    }
}
