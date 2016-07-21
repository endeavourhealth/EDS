package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Location;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LocationTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //to create the FHIR location resources, parse the Admin_OrganisationLocation file first
        //to get a map of which organisations each location belongs to
        HashMap<String, List<String>> hmLocationToOrganisation = OrganisationLocationTransformer.transform(folderPath, csvFormat);

        Admin_Location parser = new Admin_Location(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createLocation(parser, csvProcessor, csvHelper, hmLocationToOrganisation);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createLocation(Admin_Location locationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper,
                                       HashMap<String, List<String>> hmLocationToOrganisation) throws Exception {

        Location fhirLocation = new Location();
        fhirLocation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));

        String locationGuid = locationParser.getLocationGuid();
        fhirLocation.setId(locationGuid);

        if (locationParser.getDeleted()) {
            csvProcessor.deleteAdminResource(fhirLocation);
            return;
        }

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
        fhirLocation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION_ACTIVE_PERIOD, fhirPeriod));

        String name = locationParser.getLocationName();
        fhirLocation.setName(name);

        String type = locationParser.getLocationTypeDescription();
        fhirLocation.setType(CodeableConceptHelper.createCodeableConcept(type));

        String parentLocationGuid = locationParser.getParentLocationId();
        if (!Strings.isNullOrEmpty(parentLocationGuid)) {
            fhirLocation.setPartOf(csvHelper.createLocationReference(parentLocationGuid));
        }

        List<String> organisationGuids = hmLocationToOrganisation.get(locationGuid);
        if (organisationGuids != null) {
            String organisationGuid = organisationGuids.get(0);
            fhirLocation.setManagingOrganization(csvHelper.createOrganisationReference(organisationGuid));
        }

        csvProcessor.saveAdminResource(fhirLocation);
    }
}
