package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class OrganisationTransformer {

    public static HashMap<String, Organization> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<String, Organization> fhirOrganisationMap = new HashMap<>();

        Admin_Organisation parser = new Admin_Organisation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createOrganisation(parser, fhirOrganisationMap);
            }
        } finally {
            parser.close();
        }

        return fhirOrganisationMap;
    }

    private static void createOrganisation(Admin_Organisation organisationParser, HashMap<String, Organization> fhirOrganisationMap) throws Exception {

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        String orgGuid = organisationParser.getOrganisationGuid();
        fhirOrganisation.setId(orgGuid);

        //add to map for later use
        fhirOrganisationMap.put(orgGuid, fhirOrganisation);

        String odsCode = organisationParser.getODScode();
        Identifier fhirIdentifier = IdentifierHelper.createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE, odsCode);
        fhirOrganisation.addIdentifier(fhirIdentifier);


        String name = organisationParser.getOrganisatioName();
        fhirOrganisation.setName(name);

        Date openDate = organisationParser.getOpenDate();
        Date closeDate = organisationParser.getCloseDate();
        Period fhirPeriod = PeriodHelper.createPeriod(openDate, closeDate);
        fhirOrganisation.setActive(PeriodHelper.isActive(fhirPeriod));
        fhirOrganisation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ORGANISATION_ACTIVE_PERIOD, fhirPeriod));

        String parentOrganisationGuid = organisationParser.getParentOrganisationGuid();
        if (parentOrganisationGuid != null) {
            fhirOrganisation.setPartOf(ReferenceHelper.createReference(ResourceType.Organization, parentOrganisationGuid));
        }

        String ccgOrganisationGuid = organisationParser.getCCGOrganisationGuid();
        if (ccgOrganisationGuid != null) {
            fhirOrganisation.setPartOf(ReferenceHelper.createReference(ResourceType.Organization, ccgOrganisationGuid));
        }

        String type = organisationParser.getOrganisationType();
        fhirOrganisation.setType(CodeableConceptHelper.createCodeableConcept(type));
        //TODO - how to map EMIS org types to FHIR organisation types?

        String mainLocationGuid = organisationParser.getMainLocationGuid();
        Reference fhirReference = ReferenceHelper.createReference(ResourceType.Location, mainLocationGuid);
        fhirOrganisation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION, fhirReference));
    }
}