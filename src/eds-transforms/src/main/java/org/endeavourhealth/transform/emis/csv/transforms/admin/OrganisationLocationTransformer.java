package org.endeavourhealth.transform.emis.csv.transforms.admin;

import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.admin.OrganisationLocation;

import java.util.Map;

public class OrganisationLocationTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        AbstractCsvParser parser = parsers.get(OrganisationLocation.class);
        while (parser.nextRecord()) {

            try {
                createLocationOrganisationMapping((OrganisationLocation)parser, fhirResourceFiler, csvHelper);
            } catch (Exception ex) {
                throw new TransformException(parser.getCurrentState().toString(), ex);
            }
        }
    }


    private static void createLocationOrganisationMapping(OrganisationLocation parser,
                                                          FhirResourceFiler fhirResourceFiler,
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
