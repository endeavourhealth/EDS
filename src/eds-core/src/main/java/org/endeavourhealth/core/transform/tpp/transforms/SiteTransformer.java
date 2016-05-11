package org.endeavourhealth.core.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.Address;
import org.endeavourhealth.core.transform.tpp.schema.Site;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class SiteTransformer {

    public static void transform(Site tppSite, List<Resource> fhirResources) {

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ORGANIZATION));
        fhirOrganisation.setId(UUID.randomUUID().toString());
        fhirResources.add(fhirOrganisation);

        String name = tppSite.getName();
        fhirOrganisation.setName(name);

        String id = tppSite.getID();
        Identifier fhirIdentifier = Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, id, FhirUris.IDENTIFIER_SYSTEM_ODS_CODE);
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
        org.hl7.fhir.instance.model.Address fhirAddress = AddressTransformer.tranformHomeAddress(tppAddress);
        fhirOrganisation.addAddress(fhirAddress);

    }
}
