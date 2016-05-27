package org.endeavourhealth.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.tpp.schema.Address;
import org.endeavourhealth.transform.tpp.schema.Site;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class SiteTransformer {

    public static void transform(Site tppSite, boolean first, List<Resource> fhirResources) {

        //the first site is always the main organisation itself, so create an Organization resource too
        if (first) {
            createOrganisation(tppSite, fhirResources);
        }

        createLocation(tppSite, fhirResources);
    }

    private static void createLocation(Site tppSite, List<Resource> fhirResources) {

        Location fhirLocation = new Location();
        fhirLocation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_LOCATION));
        fhirLocation.setId(UUID.randomUUID().toString()); //we don't have unique IDs for these, so sub in a UUID
        fhirResources.add(fhirLocation);

        String name = tppSite.getName();
        fhirLocation.setName(name);

        //branch sites won't have national IDs and the main site will already have its ID in the Organisation
        //String id = tppSite.getID();

        String tel = tppSite.getTelephone();
        if (!Strings.isNullOrEmpty(tel)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.WORK, tel);
            fhirLocation.addTelecom(contactPoint);
        }

        String fax = tppSite.getFax();
        if (!Strings.isNullOrEmpty(fax)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.FAX,
                    ContactPoint.ContactPointUse.WORK, fax);
            fhirLocation.addTelecom(contactPoint);
        }

        String email = tppSite.getEmail();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.EMAIL,
                    ContactPoint.ContactPointUse.WORK, email);
            fhirLocation.addTelecom(contactPoint);
        }

        Address tppAddress = tppSite.getAddress();
        org.hl7.fhir.instance.model.Address fhirAddress = AddressTransformer.tranformWorkAddress(tppAddress);
        fhirLocation.setAddress(fhirAddress);

    }

    private static void createOrganisation(Site tppSite, List<Resource> fhirResources) {

        String id = tppSite.getID();

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));
        fhirOrganisation.setId(id);
        fhirResources.add(fhirOrganisation);

        String name = tppSite.getName();
        fhirOrganisation.setName(name);

        Identifier fhirIdentifier = Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, id, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
        //TODO - some sites will have local identifiers and not ODS codes. Need to differentiate somehow.
        fhirOrganisation.addIdentifier(fhirIdentifier);

        String tel = tppSite.getTelephone();
        if (!Strings.isNullOrEmpty(tel)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.WORK, tel);
            fhirOrganisation.addTelecom(contactPoint);
        }

        String fax = tppSite.getFax();
        if (!Strings.isNullOrEmpty(fax)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.FAX,
                    ContactPoint.ContactPointUse.WORK, fax);
            fhirOrganisation.addTelecom(contactPoint);
        }

        String email = tppSite.getEmail();
        if (!Strings.isNullOrEmpty(email)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.EMAIL,
                    ContactPoint.ContactPointUse.WORK, email);
            fhirOrganisation.addTelecom(contactPoint);
        }

        Address tppAddress = tppSite.getAddress();
        org.hl7.fhir.instance.model.Address fhirAddress = AddressTransformer.tranformWorkAddress(tppAddress);
        fhirOrganisation.addAddress(fhirAddress);

    }
}
