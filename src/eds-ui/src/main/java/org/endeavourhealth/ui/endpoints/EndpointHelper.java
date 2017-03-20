package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.coreui.json.JsonOrganisationManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EndpointHelper {

    public static List<JsonOrganisationManager> JsonOrganisation(List<Object[]> organisations) throws Exception {
        List<JsonOrganisationManager> ret = new ArrayList<>();

        for (Object[] OrganisationEntity : organisations) {
            String name = OrganisationEntity[0].toString();
            String alternativeName = OrganisationEntity[1]==null?"":OrganisationEntity[1].toString();
            String odsCode = OrganisationEntity[2]==null?"":OrganisationEntity[2].toString();
            String icoCode = OrganisationEntity[3]==null?"":OrganisationEntity[3].toString();
            String igToolKitStatus = OrganisationEntity[4]==null?"":OrganisationEntity[4].toString();
            String dateOfReg = OrganisationEntity[5]==null?"":OrganisationEntity[5].toString();
            String registrationPerson = OrganisationEntity[6]==null?"":OrganisationEntity[6].toString();
            String evidence = OrganisationEntity[7]==null?"":OrganisationEntity[7].toString();
            String organisationUuid = OrganisationEntity[8]==null?"":OrganisationEntity[8].toString();
            Boolean isService = (Boolean)OrganisationEntity[9];

            JsonOrganisationManager org = new JsonOrganisationManager();
            org.setName(name);
            org.setAlternativeName(alternativeName);
            org.setOdsCode(odsCode);
            org.setIcoCode(icoCode);
            org.setIgToolkitStatus(igToolKitStatus);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = simpleDateFormat.parse(dateOfReg);
            org.setDateOfRegistration(dateOfReg);
            org.setRegistrationPerson(registrationPerson);
            org.setEvidenceOfRegistration(evidence);
            org.setUuid(organisationUuid);
            if (isService)
                org.setIsService("true");
            else
                org.setIsService("false");

            ret.add(org);
        }

        return ret;
    }

    public static List<JsonRegion> JsonRegion(List<Object[]> regions) {
        List<JsonRegion> ret = new ArrayList<>();

        for (Object[] regionEntity : regions) {
            String name = regionEntity[0].toString();
            String description = regionEntity[1]==null?"":regionEntity[1].toString();
            String Uuid = regionEntity[2]==null?"":regionEntity[2].toString();

            JsonRegion reg = new JsonRegion();
            reg.setName(name);
            reg.setDescription(description);
            reg.setUuid(Uuid);

            ret.add(reg);
        }

        return ret;
    }

}
