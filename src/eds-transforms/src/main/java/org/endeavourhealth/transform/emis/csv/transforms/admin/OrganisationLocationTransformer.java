package org.endeavourhealth.transform.emis.csv.transforms.admin;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Location;
import org.endeavourhealth.transform.emis.csv.schema.Admin_OrganisationLocation;
import org.endeavourhealth.transform.fhir.AddressConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class OrganisationLocationTransformer {

    public static HashMap<String, String> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<String, String> hmLocationToOrganisation = new HashMap<>();

        Admin_OrganisationLocation parser = new Admin_OrganisationLocation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createLocationOrgansationMapping(parser, hmLocationToOrganisation);
            }
        } finally {
            parser.close();
        }

        return hmLocationToOrganisation;
    }

    private static void createLocationOrgansationMapping(Admin_OrganisationLocation organisationLocationParser, HashMap<String, String> hmLocationToOrganisation) throws Exception {

        //skip any deleted links
        if (organisationLocationParser.getDeleted()) {
            return;
        }

        String orgGuid = organisationLocationParser.getOrgansationGuid();
        String locationGuid = organisationLocationParser.getLocationGuid();

        if (hmLocationToOrganisation.get(locationGuid) != null) {
            throw new TransformException("Location " + locationGuid + " is in more than on organisation");
        }

        hmLocationToOrganisation.put(locationGuid, orgGuid);
    }
}
