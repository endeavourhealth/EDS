package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Admin_OrganisationLocation;

import java.util.*;

public class OrganisationLocationTransformer {

    public static HashMap<String, List<String>> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<String, List<String>> hmLocationToOrganisation = new HashMap<>();

        Admin_OrganisationLocation parser = new Admin_OrganisationLocation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createLocationOrgansationMapping(parser, hmLocationToOrganisation);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

        return hmLocationToOrganisation;
    }

    private static void createLocationOrgansationMapping(Admin_OrganisationLocation organisationLocationParser,
                                                         HashMap<String, List<String>> hmLocationToOrganisation) throws Exception {

        //if an org-location link has been deleted, then either a) the location has been deleted
        //in which case we'll sort it out because we'll have the deleted row in the Location CSV or
        //b) it's now part of a new organisation, in which case we'll have a new non-deleted row
        //in this CSV. In both cases, it's safe to simply ignore the delted records in this file.
        if (organisationLocationParser.getDeleted()) {
            return;
        }

        String orgGuid = organisationLocationParser.getOrgansationGuid();
        String locationGuid = organisationLocationParser.getLocationGuid();
        boolean mainLocation = organisationLocationParser.getIsMainLocation();
        //the MainLocation field is duplicated from the Organisation CSV file and is processed from that file

        List<String> orgGuids = hmLocationToOrganisation.get(locationGuid);
        if (orgGuids == null) {
            orgGuids = new ArrayList<>();
            hmLocationToOrganisation.put(locationGuid, orgGuids);
        }

        //if this location link is for the main location of an organisation, then insert that
        //org at the start of the list, so it's used as the managing organisation for the location
        if (mainLocation) {
            orgGuids.add(0, orgGuid);
        } else {
            orgGuids.add(orgGuid);
        }

    }
}
