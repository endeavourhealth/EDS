package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.emis.csv.schema.Admin_UserInRole;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class UserInRoleTransformer {

    public static HashMap<UUID, Practitioner> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<UUID, Practitioner> fhirUsersInRoleMap = new HashMap<>();

        Admin_UserInRole userInRoleParser = new Admin_UserInRole(folderPath, csvFormat);
        try {
            while (userInRoleParser.nextRecord()) {
                createPractitioner(userInRoleParser, fhirUsersInRoleMap);
            }
        } finally {
            userInRoleParser.close();
        }

        return fhirUsersInRoleMap;
    }

    private static void createPractitioner(Admin_UserInRole userInRoleParser, HashMap<UUID, Practitioner> fhirUsersInRoleMap) throws Exception {

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PRACTITIONER));

        UUID userInRoleGuid = userInRoleParser.getUserInRoleGuid();
        fhirUsersInRoleMap.put(userInRoleGuid, fhirPractitioner);

        fhirPractitioner.setId(userInRoleGuid.toString());

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

        UUID orgUuid = userInRoleParser.getOrganisationGuid();
        fhirRole.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, orgUuid.toString()));

        String roleName = userInRoleParser.getJobCategoryName();
        fhirRole.setRole(new CodeableConcept().setText(roleName));
    }
}
