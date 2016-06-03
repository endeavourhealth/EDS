package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Patient;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OrganisationTransformer {

    public static HashMap<UUID, Organization> transformOrganisations(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<UUID, Organization> fhirOrganisationMap = new HashMap<>();

        Admin_Organisation organisationParser = new Admin_Organisation(folderPath, csvFormat);
        try {
            while (organisationParser.nextRecord()) {
                transformOrganisation(organisationParser, fhirOrganisationMap);
            }
        } finally {
            organisationParser.close();
        }

        return fhirOrganisationMap;
    }

    private static void transformOrganisation(Admin_Organisation organisationParser, HashMap<UUID, Organization> fhirOrganisationMap) throws Exception {

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        UUID orgGuid = organisationParser.getOrganisationGuid();
        fhirOrganisation.setId(orgGuid.toString());

        //add to map for later use
        fhirOrganisationMap.put(orgGuid, fhirOrganisation);

        String odsCode = organisationParser.getODScode();
        Identifier fhirIdentifier = Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE, odsCode);
        fhirOrganisation.addIdentifier(fhirIdentifier);


        String name = organisationParser.getOrganisatioName();
        fhirOrganisation.setName(name);

        Date openDate = organisationParser.getOpenDate();
        Date closeDate = organisationParser.getCloseDate();
        Period fhirPeriod = Fhir.createPeriod(openDate, closeDate);
        fhirOrganisation.setActive(Fhir.isActive(fhirPeriod));
        fhirOrganisation.addExtension(Fhir.createExtension(FhirExtensionUri.ORGANISATION_ACTIVE_PERIOD, fhirPeriod));

        UUID parentOrganisationGuid = organisationParser.getParentOrganisationGuid();
        if (parentOrganisationGuid != null) {
            fhirOrganisation.setPartOf(Fhir.createReference(ResourceType.Organization, parentOrganisationGuid.toString()));
        }

        UUID ccgOrganisationGuid = organisationParser.getCCGOrganisationGuid();
        if (ccgOrganisationGuid != null) {
            fhirOrganisation.setPartOf(Fhir.createReference(ResourceType.Organization, ccgOrganisationGuid.toString()));
        }

        String type = organisationParser.getOrganisationType();
        fhirOrganisation.setType(Fhir.createCodeableConcept(type));
        //TODO - how to map EMIS org types to FHIR organisation types?

        UUID mainLocationGuid = organisationParser.getMainLocationGuid();
        Reference fhirReference = Fhir.createReference(ResourceType.Location, mainLocationGuid.toString());
        fhirOrganisation.addExtension(Fhir.createExtension(FhirExtensionUri.LOCATION, fhirReference));
    }
}