package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SD317 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD307.class);

    /**
     * finds affected TPP services for SD-317
     */
    public static void findTppServicesMissingClinicalData(boolean verbose, String odsCodeRegex) {
        LOG.debug("Finding TPP Services Missing Clinical Data " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            File dstFile = new File("SD317_results.csv");
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            CSVFormat format = CSVFormat.DEFAULT
                    .withHeader("Name", "ODS Code", "Last Patient", "Diff Patient", "Last Code", "Diff Code", "Last Medication", "Diff Medication"
                    );
            CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);
                findTppServicesMissingClinicalDataForService(verbose, service, printer);
            }

            printer.close();

            LOG.debug("Finished Finding TPP Services Missing Clinical Data " + odsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void findTppServicesMissingClinicalDataForService(boolean verbose, Service service, CSVPrinter printer) throws Exception {

        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No emis endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        Date lastPatientFile = null;
        Date lastCodeFile = null;
        Date lastMedicationFile = null;

        //exchange list is most-recent-first, so go backwards
        for (int i = exchanges.size() - 1; i >= 0; i--) {
            Exchange exchange = exchanges.get(i);
            if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                continue;
            }

            Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);

            String filePath = findFilePathInExchange(exchange, "Patient");
            if (!Strings.isNullOrEmpty(filePath)) {
                lastPatientFile = dataDate;
            }

            filePath = findFilePathInExchange(exchange, "Code");
            if (!Strings.isNullOrEmpty(filePath)) {
                lastCodeFile = dataDate;
            }

            filePath = findFilePathInExchange(exchange, "PrimaryCareMedication");
            if (!Strings.isNullOrEmpty(filePath)) {
                lastMedicationFile = dataDate;
            }
        }

        printer.printRecord(
                service.getName(),
                service.getLocalId(),
                dateFormat(lastPatientFile),
                dateDiff(lastPatientFile),
                dateFormat(lastCodeFile),
                dateDiff(lastCodeFile),
                dateFormat(lastMedicationFile),
                dateDiff(lastMedicationFile)
        );

        //"Name", "ODS Code", "Last Patient", "Diff Patient", "Last Code", "Diff Code", "Last Medication", "Diff Medication"
    }

    private static String dateDiff(Date d) {
        if (d == null) {
            return "MAX";
        }

        Date now = new Date();
        long msDiff = now.getTime() - d.getTime();
        long dayDiff = msDiff / (1000 * 60 * 60 * 24);
        return "" + dayDiff;
    }

    private static String dateFormat(Date d) {
        if (d == null) {
            return "NULL";
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(d);
    }
}
