package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Location;
import org.endeavourhealth.transform.emis.csv.schema.Admin_OrganisationLocation;
import org.endeavourhealth.transform.fhir.AddressConverter;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class OrganisationLocationTransformer {

    public static HashMap<UUID, UUID> transformOrganisationLocations(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<UUID, UUID> hmLocationToOrganisation = new HashMap<>();

        Admin_OrganisationLocation organisationLocationParser = new Admin_OrganisationLocation(folderPath, csvFormat);
        try {
            while (organisationLocationParser.nextRecord()) {
                transformOrganisationLocation(organisationLocationParser, hmLocationToOrganisation);
            }
        } finally {
            organisationLocationParser.close();
        }

        return hmLocationToOrganisation;
    }

    private static void transformOrganisationLocation(Admin_OrganisationLocation organisationLocationParser, HashMap<UUID, UUID> hmLocationToOrganisation) throws Exception {

        //skip any deleted links
        if (organisationLocationParser.getDeleted()) {
            return;
        }

        UUID orgGuid = organisationLocationParser.getOrgansationGuid();
        UUID locationGuid = organisationLocationParser.getLocationGuid();

        if (hmLocationToOrganisation.get(locationGuid) != null) {
            throw new TransformException("Location " + locationGuid + " is in more than on organisation");
        }

        hmLocationToOrganisation.put(locationGuid, orgGuid);
    }
}
