package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.admin.OrganisationLocation;

import java.util.Map;

public class OrganisationLocationTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvTransformer> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        OrganisationLocation parser = (OrganisationLocation)parsers.get(OrganisationLocation.class);

        while (parser.nextRecord()) {

            try {
                createLocationOrgansationMapping(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createLocationOrgansationMapping(OrganisationLocation parser,
                                                         CsvProcessor csvProcessor,
                                                         EmisCsvHelper csvHelper) throws Exception {

        //if an org-location link has been deleted, then either a) the location has been deleted
        //in which case we'll sort it out because we'll have the deleted row in the Location CSV or
        //b) it's now part of a new organisation, in which case we'll have a new non-deleted row
        //in this CSV. In both cases, it's safe to simply ignore the deleted records in this file.
        if (parser.getDeleted()) {
            return;
        }

        String orgGuid = parser.getOrgansationGuid();
        String locationGuid = parser.getLocationGuid();
        boolean mainLocation = parser.getIsMainLocation();

        csvHelper.cacheOrganisationLocationMap(locationGuid, orgGuid, mainLocation);
    }
}
