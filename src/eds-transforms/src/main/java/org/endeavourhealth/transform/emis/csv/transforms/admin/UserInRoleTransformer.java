package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.emis.csv.schema.Admin_UserInRole;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class UserInRoleTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Admin_UserInRole userInRoleParser = new Admin_UserInRole(folderPath, csvFormat);
        try {
            while (userInRoleParser.nextRecord()) {
                createPractitioner(userInRoleParser, csvProcessor, csvHelper);
            }
        } finally {
            userInRoleParser.close();
        }
    }

    private static void createPractitioner(Admin_UserInRole userInRoleParser,
                                           CsvProcessor csvProcessor,
                                           EmisCsvHelper csvHelper) throws Exception {

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PRACTITIONER));

        String userInRoleGuid = userInRoleParser.getUserInRoleGuid();
        fhirPractitioner.setId(userInRoleGuid);

        String title = userInRoleParser.getTitle();
        String givenName = userInRoleParser.getGivenName();
        String surname = userInRoleParser.getSurname();

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
        fhirRole.setRole(new CodeableConcept().setText(roleName));

        csvProcessor.saveAdminResource(fhirPractitioner);
    }
}
