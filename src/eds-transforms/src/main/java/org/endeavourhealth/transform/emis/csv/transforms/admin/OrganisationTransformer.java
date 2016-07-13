package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.schema.OrganisationType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class OrganisationTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Admin_Organisation parser = new Admin_Organisation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createOrganisation(parser, csvProcessor, csvHelper);
            }
        } finally {
            parser.close();
        }
    }

    private static void createOrganisation(Admin_Organisation organisationParser,
                                           CsvProcessor csvProcessor,
                                           EmisCsvHelper csvHelper) throws Exception {

        Organization fhirOrganisation = new Organization();
        fhirOrganisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        String orgGuid = organisationParser.getOrganisationGuid();
        fhirOrganisation.setId(orgGuid);

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
            fhirOrganisation.setPartOf(csvHelper.createOrganisationReference(parentOrganisationGuid));
        }

        String ccgOrganisationGuid = organisationParser.getCCGOrganisationGuid();
        if (ccgOrganisationGuid != null) {
            fhirOrganisation.setPartOf(csvHelper.createOrganisationReference(ccgOrganisationGuid));
        }

        OrganisationType fhirOrgType = convertOrganisationType(organisationParser.getOrganisationType());
        fhirOrganisation.setType(CodeableConceptHelper.createCodeableConcept(fhirOrgType));

        String mainLocationGuid = organisationParser.getMainLocationGuid();
        Reference fhirReference = csvHelper.createLocationReference(mainLocationGuid);
        fhirOrganisation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION, fhirReference));

        csvProcessor.saveAdminResource(fhirOrganisation);
    }

    private static OrganisationType convertOrganisationType(String csvOrganisationType) {
        return OrganisationType.fromDescription(csvOrganisationType);
    }
}