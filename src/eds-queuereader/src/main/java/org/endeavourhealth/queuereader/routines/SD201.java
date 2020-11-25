package org.endeavourhealth.queuereader.routines;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.*;

public class SD201 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD201.class);

    /**
     * sees what ODS has for org types for all DDS services
     */
    public static void checkOrgOdsTypes() {
        LOG.debug("Checking ODS for Org Types");
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();


            String outputFile = "SD201_ods_org_types.csv";
            PrintWriter fw = new PrintWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fw);
            CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                    .withHeader("ods_code", "dds_org", "org_type_1", "org_type_2", "org_type_3", "org_type_4", "org_type_5", "org_type_6", "org_type_7", "org_type_8", "org_type_9", "org_type_10"
                    );
            CSVPrinter printer = new CSVPrinter(bw, format);

            Set<String> odsCodesDone = new HashSet<>();

            for (Service service: services) {

                LOG.debug("Checking " + service);
                String localId = service.getLocalId();
                checkOds(localId, printer, true, odsCodesDone);
            }

            printer.close();

            LOG.debug("Finished Checking ODS for Org Types to " + outputFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void checkOds(String odsCode, CSVPrinter printer, boolean ddsOrg, Set<String> odsCodesDone) throws Exception {

        if (odsCodesDone.contains(odsCode)) {
            return;
        }
        odsCodesDone.add(odsCode);

        OdsOrganisation odsOrg = OdsWebService.lookupOrganisationViaRest(odsCode);
        if (odsOrg == null) {
            return;
        }

        Set<OrganisationType> types = odsOrg.getOrganisationTypes();
        List<OrganisationType> list = new ArrayList<>(types);
        list.sort(((o1, o2) -> o1.getDescription().compareToIgnoreCase(o2.getDescription())));

        List<String> record = new ArrayList<>();
        record.add(odsCode);
        record.add("" + ddsOrg);
        for (OrganisationType type: list) {
            record.add(type.getDescription());
        }
        while (record.size() < 12) {
            record.add("");
        }

        printer.printRecord(record);

        //do parents
        Map<String, OdsOrganisation> parents = odsOrg.getParents();
        for (OdsOrganisation parent: parents.values()) {
            String parentOdsCode = parent.getOdsCode();
            checkOds(parentOdsCode, printer, false, odsCodesDone);
        }

    }
}
