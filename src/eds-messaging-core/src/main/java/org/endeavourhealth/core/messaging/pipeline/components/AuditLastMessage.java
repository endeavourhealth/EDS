package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.AuditLastMessageConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.LastDataToSubscriber;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.transform.common.ExchangeHelper;

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
            LastDataToSubscriber a = new LastDataToSubscriber();

            SubscriberBatch subscriberBatch = SubscriberBatch.getSubscriberBatch(exchange);
            String subscriberName = subscriberBatch.getEndpoint();
            UUID serviceId = exchange.getServiceId();
            UUID systemId = exchange.getSystemId();
            UUID exchangeId = exchange.getId();
            Date lastDataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);

            //won't always be present on really old exchanges or "special" ones created for non-standard transformations
            if (lastDataDate == null) {
                return;
            }

            a.setServiceId(serviceId);
            a.setSystemId(systemId);
            a.setSubscriberConfigName(subscriberName);
            a.setDataDate(lastDataDate);
            a.setSentDate(new Date());
            a.setExchangeId(exchangeId);

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            exchangeDal.save(a);


        } catch (Exception ex) {
            throw new PipelineException("Failed to save LastDataToSubscriber audit", ex);
        }
    }
}
