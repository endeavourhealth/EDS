package org.endeavourhealth.core.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.*;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class UserTransformer {

    public static void transform(User tppUser, List<Resource> fhirResources) {

        String userName = tppUser.getUserName();

        Practitioner fhirPractitioner = new Practitioner();
        fhirPractitioner.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_PRACTITIONER));
        fhirPractitioner.setId(userName); //user name is unique, so use as the ID
        fhirResources.add(fhirPractitioner);

        String title = tppUser.getTitle();
        String firstName = tppUser.getFirstName();
        String middleNames = tppUser.getMiddleNames();
        String surname = tppUser.getSurname();
        HumanName fhirName = Fhir.createHumanName(HumanName.NameUse.OFFICIAL, title, firstName, middleNames, surname);
        fhirPractitioner.setName(fhirName);

        Sex tppSex = tppUser.getGender();
        fhirPractitioner.setGender(SexTransformer.transform(tppSex));

        String role = tppUser.getRole();
        //String roleCode = tppUser.getRoleCode(); //the textual role desc is sufficient

        Practitioner.PractitionerPractitionerRoleComponent fhirRole = fhirPractitioner.addPractitionerRole();
        fhirRole.setRole(new CodeableConcept().setText(role));

        String nationalId = tppUser.getNationalID();
        NationalIDType nationalIdType = tppUser.getNationalIDType();
        if (!Strings.isNullOrEmpty(nationalId)) {
            if (nationalIdType == NationalIDType.GMC) {
                Identifier fhirIdentifier = Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, nationalId, FhirUris.IDENTIFIER_SYSTEM_GMC_NUMBER);
                fhirPractitioner.addIdentifier(fhirIdentifier);
            } else {
                //TODO - need to work out enumeration of possible options
            }
        }

        //access rights aren't relevant to the patient record
        //AccessRights tppAccessRights = tppUser.getAccessRights();

        //user groups aren't relevant to the patient record
        //UserGroups tppUserGroups = tppUser.getUserGroups();
    }
}
