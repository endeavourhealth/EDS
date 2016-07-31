package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.OrganisationType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

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
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
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
        if (!Strings.isNullOrEmpty(parentOrganisationGuid)) {
            fhirOrganisation.setPartOf(csvHelper.createOrganisationReference(parentOrganisationGuid));
        }

        String ccgOrganisationGuid = organisationParser.getCCGOrganisationGuid();
        if (!Strings.isNullOrEmpty(ccgOrganisationGuid)) {
            fhirOrganisation.setPartOf(csvHelper.createOrganisationReference(ccgOrganisationGuid));
        }

        String organisationType = organisationParser.getOrganisationType();
        OrganisationType fhirOrgType = convertOrganisationType(organisationType);
        if (fhirOrgType != null) {
            fhirOrganisation.setType(CodeableConceptHelper.createCodeableConcept(fhirOrgType));
        } else {
            //if the org type from the CSV can't be mapped to one of the value set, store as a freetext type
            fhirOrganisation.setType(CodeableConceptHelper.createCodeableConcept(organisationType));
        }

        String mainLocationGuid = organisationParser.getMainLocationGuid();
        Reference fhirReference = csvHelper.createLocationReference(mainLocationGuid);
        fhirOrganisation.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION, fhirReference));

        csvProcessor.saveAdminResource(fhirOrganisation);
    }

    private static OrganisationType convertOrganisationType(String csvOrganisationType) {
        try {
            return OrganisationType.fromDescription(csvOrganisationType);
        } catch (Exception ex) {

            //the below mappings are based on what was present in the EMIS CSV sample files
            //EMIS has been asked for a complete list, but until this is made available, these
            //are the only known types. There are a number of organisation types, such as "Hospice"
            //or "Community" which don't map to any official NHS organisation type
            if (csvOrganisationType.equalsIgnoreCase("General Practice")
                || csvOrganisationType.equalsIgnoreCase("General Practice Surgery")
                || csvOrganisationType.equalsIgnoreCase("Main Surgery")) {
                return OrganisationType.GP_PRACTICE;

            } else if (csvOrganisationType.equalsIgnoreCase("CCG")) {
                return OrganisationType.CCG;

            } else if (csvOrganisationType.equalsIgnoreCase("PCT Site")
                    || csvOrganisationType.equalsIgnoreCase("Primary Care Trust")) {
                return OrganisationType.PCT;

            } else if (csvOrganisationType.equalsIgnoreCase("Hospital")
                    || csvOrganisationType.equalsIgnoreCase("NHS Trust Site")
                    || csvOrganisationType.equalsIgnoreCase("NHS Trust")) {
                return OrganisationType.NHS_TRUST;

            } else {
                return null;
            }
        }

    }
}