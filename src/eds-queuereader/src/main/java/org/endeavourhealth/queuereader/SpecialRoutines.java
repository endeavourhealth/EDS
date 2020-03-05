package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class SpecialRoutines {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialRoutines.class);

    public static void findOutOfOrderTppServices() {
        LOG.info("Finding Out of Order TPP Services");
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {
                if (service.getTags() == null
                        && !service.getTags().containsKey("TPP")) {
                    continue;
                }

                LOG.info("Checking " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");

                    //exchanges are in insert date order, most recent first
                    Date previousDate = null;

                    for (int i=0; i<exchanges.size(); i++) {
                        Exchange exchange = exchanges.get(i);

                        Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
                        if (dataDate == null) {
                            throw new Exception("No data date for exchange " + exchange.getId());
                        }

                        if (previousDate == null
                                || dataDate.before(previousDate)) {
                            previousDate = dataDate;

                        } else {
                            LOG.warn("Exchange " + exchange.getId() + " from " + exchange.getTimestamp() + " is out of order");
                        }
                    }

                }
            }

            //find TPP services
            //get exchanges
            //work from MOST recent
            //see if exchanges have data date out of order
            //how to fix?...
            //If queued up -
            //If already processed - move exchange and re-queued from AFTER bulk
            //If not processed & not queued - just move exchange?


            LOG.info("Finished Finding Out of Order TPP Services");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
