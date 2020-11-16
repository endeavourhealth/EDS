package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeEvent;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class SD203 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD203.class);

    public static void findEmisExchangesNotProcessed(String odsCodeRegex) {
        LOG.debug("Finding Emis Services that Need Re-processing for " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("Wrong number of system IDs for " + service);
                }
                UUID systemId = systemIds.get(0);

                String publisherStatus = null;
                for (ServiceInterfaceEndpoint serviceInterface: service.getEndpointsList()) {
                    if (serviceInterface.getSystemUuid().equals(systemId)) {
                        publisherStatus = serviceInterface.getEndpoint();
                    }
                }

                if (publisherStatus == null) {
                    throw new Exception("Failed to find publisher status for service " + service);
                }

                LOG.debug("");
                LOG.debug("CHECKING " + service + " " + publisherStatus + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                if (publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL)) {
                    LOG.debug("Skipping service because set to auto-fail");
                    continue;
                }

                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                for (int i=exchanges.size()-1; i>=0; i--) {
                    Exchange exchange = exchanges.get(i);

                    //if can't be queued, ignore it
                    Boolean allowQueueing = exchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
                    if (allowQueueing != null
                            && !allowQueueing.booleanValue()) {
                        continue;
                    }

                    //skip any custom extracts as I know that some explicitly were skipped when we had multiple queued up, and just did the most recent
                    String body = exchange.getBody();
                    List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body, false);
                    if (files.size() <= 1) {
                        continue;
                    }

                    //LOG.debug("Doing exchange " + exchange.getId() + " from " + exchange.getHeaderAsDate(HeaderKeys.DataDate));

                    List<ExchangeTransformAudit> audits = exchangeDal.getAllExchangeTransformAudits(service.getId(), systemId, exchange.getId());
                    List<ExchangeEvent> events = exchangeDal.getExchangeEvents(exchange.getId());

                    //was it transformed OK before it was re-queued with filtering?
                    boolean transformedWithoutFiltering = false;
                    List<String> logging = new ArrayList<>();

                    for (ExchangeTransformAudit audit: audits) {

                        //transformed OK
                        boolean transformedOk = audit.getEnded() != null && audit.getErrorXml() == null;
                        if (!transformedOk) {
                            logging.add("Audit " + audit.getStarted() + " didn't complete OK, so not counting");
                            continue;
                        }

                        //if transformed OK see whether filtering was applied BEFORE
                        Date dtTransformStart = audit.getStarted();
                        logging.add("Audit completed OK from " + audit.getStarted());

                        //find immediately proceeding event showing loading into inbound queue
                        ExchangeEvent previousLoadingEvent = null;
                        for (int j=events.size()-1; j>=0; j--) {
                            ExchangeEvent event = events.get(j);
                            Date dtEvent = event.getTimestamp();
                            if (dtEvent.after(dtTransformStart)) {
                                logging.add("Ignoring event from " + dtEvent + " as AFTER transform");
                                continue;
                            }

                            String eventDesc = event.getEventDesc();
                            if (eventDesc.startsWith("Manually pushed into edsInbound exchange")
                                    || eventDesc.startsWith("Manually pushed into EdsInbound exchange")) {
                                previousLoadingEvent = event;
                                logging.add("Proceeding event from " + dtEvent + " [" + eventDesc + "]");
                                break;
                            } else {
                                logging.add("Ignoring event from " + dtEvent + " as doesn't match text [" + eventDesc + "]");
                            }
                        }

                        if (previousLoadingEvent == null) {
                            //if transformed OK and no previous manual loading event, then it was OK
                            transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK without being manually loaded = OK");

                        } else {
                            //if transformed OK and was manually loaded into queue, then see if event applied filtering or not
                            String eventDesc = previousLoadingEvent.getEventDesc();
                            if (!eventDesc.contains("Filtered on file types")) {
                                transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK and was manually loaded without filtering = OK");

                            } else {
                                logging.add("Event desc filters on file types, so DIDN'T transform OK");
                            }
                        }
                    }

                    if (!transformedWithoutFiltering) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        LOG.error("" + service + " -> exchange " + exchange.getId() + " from " + sdf.format(exchange.getHeaderAsDate(HeaderKeys.DataDate)));
                    }
                }
            }

            LOG.debug("Finished Finding Emis Services that Need Re-processing");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }
}
