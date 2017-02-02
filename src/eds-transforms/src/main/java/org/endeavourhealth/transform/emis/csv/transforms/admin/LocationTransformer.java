package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.admin.Location;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LocationTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Location.class);
        while (parser.nextRecord()) {

            try {
                createResource((Location)parser, fhirResourceFiler, csvHelper);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }


    private static void createResource(Location parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper) throws Exception {

        org.hl7.fhir.instance.model.Location fhirLocation = new org.hl7.fhir.instance.model.Location();
        fhirLocation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));

        String locationGuid = parser.getLocationGuid();
        fhirLocation.setId(locationGuid);

        if (parser.getDeleted()) {
            fhirResourceFiler.deleteAdminResource(parser.getCurrentState(), fhirLocation);

            //this resource exists in our admin resource cache, so we can populate the
            //main database when new practices come on, so we need to update that too
            csvHelper.deleteAdminResourceFromCache(fhirLocation);
            return;
        }

        String houseNameFlat = parser.getHouseNameFlatNumber();
        String numberAndStreet = parser.getNumberAndStreet();
        String village = parser.getVillage();
        String town = parser.getTown();
        String county = parser.getCounty();
        String postcode = parser.getPostcode();

        Address fhirAddress = AddressConverter.createAddress(Address.AddressUse.WORK, houseNameFlat, numberAndStreet, village, town, county, postcode);
        fhirLocation.setAddress(fhirAddress);

        String phoneNumber = parser.getPhoneNumber();
        if (!Strings.isNullOrEmpty(phoneNumber)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, phoneNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String faxNumber = parser.getFaxNumber();
        if (!Strings.isNullOrEmpty(faxNumber)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.FAX, ContactPoint.ContactPointUse.WORK, faxNumber);
            fhirLocation.addTelecom(fhirContact);
        }

        String email = parser.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint fhirContact = ContactPointHelper.createContactPoint(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.WORK, email);
            fhirLocation.addTelecom(fhirContact);
        }

        Date openDate = parser.getOpenDate();
        Date closeDate = parser.getCloseDate();
        boolean deleted = parser.getDeleted();
        Period fhirPeriod = PeriodHelper.createPeriod(openDate, closeDate);
        if (PeriodHelper.isActive(fhirPeriod) && !deleted) {
            fhirLocation.setStatus(org.hl7.fhir.instance.model.Location.LocationStatus.ACTIVE);
        } else {
            fhirLocation.setStatus(org.hl7.fhir.instance.model.Location.LocationStatus.INACTIVE);
        }
        fhirLocation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ACTIVE_PERIOD, fhirPeriod));

        String mainContactName = parser.getMainContactName();
        if (!Strings.isNullOrEmpty(mainContactName)) {
            fhirLocation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION_MAIN_CONTACT, new StringType(mainContactName)));
        }

        String name = parser.getLocationName();
        fhirLocation.setName(name);

        String type = parser.getLocationTypeDescription();
        fhirLocation.setType(CodeableConceptHelper.createCodeableConcept(type));

        String parentLocationGuid = parser.getParentLocationId();
        if (!Strings.isNullOrEmpty(parentLocationGuid)) {
            fhirLocation.setPartOf(csvHelper.createLocationReference(parentLocationGuid));
        }

        List<String> organisationGuids = csvHelper.findOrganisationLocationMapping(locationGuid);
        if (organisationGuids != null) {
            String organisationGuid = organisationGuids.get(0);
            fhirLocation.setManagingOrganization(csvHelper.createOrganisationReference(organisationGuid));
        }

        fhirResourceFiler.saveAdminResource(parser.getCurrentState(), fhirLocation);

        //this resource exists in our admin resource cache, so we can populate the
        //main database when new practices come on, so we need to update that too
        csvHelper.saveAdminResourceToCache(fhirLocation);
    }
}
