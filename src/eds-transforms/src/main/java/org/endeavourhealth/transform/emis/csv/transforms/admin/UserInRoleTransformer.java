package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.admin.UserInRole;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.Date;
import java.util.Map;

public class UserInRoleTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        UserInRole parser = (UserInRole)parsers.get(UserInRole.class);

        while (parser.nextRecord()) {

            try {
                createResource(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createResource(UserInRole parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PRACTITIONER));

        String userInRoleGuid = parser.getUserInRoleGuid();
        fhirPractitioner.setId(userInRoleGuid);

        String title = parser.getTitle();
        String givenName = parser.getGivenName();
        String surname = parser.getSurname();

        //the sample data contains users with a given name but no surname. FHIR requires all names
        //to have a surname, so treat the sole given name as the surname
        if (Strings.isNullOrEmpty(surname)) {
            surname = givenName;
            givenName = "";
        }

        //in the EMIS test pack, we have at least one record with no name details at all, so need to handle it
        if (Strings.isNullOrEmpty(surname)) {
            surname = "Unknown";
        }

        fhirPractitioner.setName(NameConverter.convert(givenName, surname, title));

        Date startDate = parser.getContractStartDate();
        Date endDate = parser.getContractEndDate();
        Period fhirPeriod = PeriodHelper.createPeriod(startDate, endDate);
        boolean active = PeriodHelper.isActive(fhirPeriod);

        fhirPractitioner.setActive(active);

        Practitioner.PractitionerPractitionerRoleComponent fhirRole = fhirPractitioner.addPractitionerRole();

        fhirRole.setPeriod(fhirPeriod);

        String orgUuid = parser.getOrganisationGuid();
        fhirRole.setManagingOrganization(csvHelper.createOrganisationReference(orgUuid));

        String roleName = parser.getJobCategoryName();
        String roleCode = parser.getJobCategoryCode();
        fhirRole.setRole(CodeableConceptHelper.createCodeableConcept(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES, roleName, roleCode));

        csvProcessor.saveAdminResource(parser.getCurrentState(), fhirPractitioner);
    }
}
