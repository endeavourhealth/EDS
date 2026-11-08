package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.AuditLastMessageConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.LastDataInSubscriber;
import org.endeavourhealth.core.database.dal.audit.models.LastDataToSubscriber;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;

import java.util.Date;
import java.util.UUID;

public class AuditLastMessage extends PipelineComponent {
    private AuditLastMessageConfig config;

    public AuditLastMessage(AuditLastMessageConfig config) {
        this.config = config;
    }

    @Override
    protected void process(Exchange exchange) throws PipelineException {

        //if the exchange doesn't have the flag, return out
        if (!ExchangeHelper.isLastMessage(exchange)) {
            return;
        }

        //if the exchange is flagged as "do not re-queue" then return out as it's not part of the normal data pipeline
        if (!ExchangeHelper.isAllowRequeueing(exchange)) {
            return;
        }

        try {

            SubscriberBatch subscriberBatch = SubscriberBatch.getSubscriberBatch(exchange);
            String subscriberName = subscriberBatch.getEndpoint();
            UUID serviceId = exchange.getServiceId();
            UUID systemId = exchange.getSystemId();
            UUID exchangeId = exchange.getId();
            Date extractDate = exchange.getHeaderAsDate(HeaderKeys.ExtractDate);
            Date extractCutoff = exchange.getHeaderAsDate(HeaderKeys.ExtractCutoff);
            boolean hasPatientData = exchange.getHeaderAsBoolean(HeaderKeys.HasPatientData, true); //this header is only set when FALSE, so default to true otherwise

            //if we don't have dates or the extract doesn't contain any patient data, then don't update the audit
            if (extractDate == null
                    || extractCutoff == null
                    || !hasPatientData) {
                return;
            }

            LastDataDalI dal = DalProvider.factoryLastDataDal();

            //audit that we've sent the data
            LastDataToSubscriber lastSent = new LastDataToSubscriber();
            lastSent.setServiceId(serviceId);
            lastSent.setSystemId(systemId);
            lastSent.setSubscriberConfigName(subscriberName);
            lastSent.setSentDate(new Date());
            lastSent.setExchangeId(exchangeId);
            lastSent.setExtractDate(extractDate);
            lastSent.setExtractCutoff(extractCutoff);
            dal.save(lastSent);

            LastDataInSubscriber lastWritten = new LastDataInSubscriber();
            lastWritten.setServiceId(serviceId);
            lastWritten.setSystemId(systemId);
            lastWritten.setSubscriberConfigName(subscriberName);
            lastWritten.setWrittenDate(new Date());
            lastWritten.setExchangeId(exchangeId);
            lastWritten.setExtractDate(extractDate);
            lastWritten.setExtractCutoff(extractCutoff);

            //for internal subscribers we will already have written the data to the DB, but
            //for remote ones, we'll just have staged it for later collection, so audit that
            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberName);
            if (subscriberConfig.getRemoteSubscriberId() == null) {
                //if internal, just audit that we're now fully up to date
                dal.save(lastWritten);

            } else {
                //if remote, we need to tell the data_generator DB that we're waiting on the specific queued message
                //and that will get updated later when the Remote Subscriber has actually applied the data
                UUID queuedMessageId = subscriberBatch.getQueuedMessageId();
                int subscriberId = subscriberConfig.getRemoteSubscriberId().intValue();

                SubscriberZipFileUUIDsDalI dataGeneratorDal = DalProvider.factorySubscriberZipFileUUIDs();
                dataGeneratorDal.logLastMessageStaged(subscriberId, queuedMessageId, lastWritten);
            }


        } catch (Exception ex) {
            throw new PipelineException("Failed to save LastDataToSubscriber audit", ex);
        }
    }
}
