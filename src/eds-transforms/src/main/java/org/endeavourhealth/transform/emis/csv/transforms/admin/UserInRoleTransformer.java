package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.admin.UserInRole;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.Date;

public class UserInRoleTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        UserInRole parser = new UserInRole(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createPractitioner(parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createPractitioner(UserInRole userInRoleParser,
                                           CsvProcessor csvProcessor,
                                           EmisCsvHelper csvHelper) throws Exception {

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PRACTITIONER));

        String userInRoleGuid = userInRoleParser.getUserInRoleGuid();
        fhirPractitioner.setId(userInRoleGuid);

        String title = userInRoleParser.getTitle();
        String givenName = userInRoleParser.getGivenName();
        String surname = userInRoleParser.getSurname();

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

        Date startDate = userInRoleParser.getContractStartDate();
        Date endDate = userInRoleParser.getContractEndDate();
        Period fhirPeriod = PeriodHelper.createPeriod(startDate, endDate);
        boolean active = PeriodHelper.isActive(fhirPeriod);

        fhirPractitioner.setActive(active);

        Practitioner.PractitionerPractitionerRoleComponent fhirRole = fhirPractitioner.addPractitionerRole();

        fhirRole.setPeriod(fhirPeriod);

        String orgUuid = userInRoleParser.getOrganisationGuid();
        fhirRole.setManagingOrganization(csvHelper.createOrganisationReference(orgUuid));

        String roleName = userInRoleParser.getJobCategoryName();
        String roleCode = userInRoleParser.getJobCategoryCode();
        fhirRole.setRole(CodeableConceptHelper.createCodeableConcept(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES, roleName, roleCode));

        csvProcessor.saveAdminResource(fhirPractitioner);
    }
}
