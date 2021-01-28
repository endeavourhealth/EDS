package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.AuditLastMessageConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
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
            Date extractDate = exchange.getHeaderAsDate(HeaderKeys.ExtractDate);
            Date extractCutoff = exchange.getHeaderAsDate(HeaderKeys.ExtractCutoff);
            boolean hasPatientData = exchange.getHeaderAsBoolean(HeaderKeys.HasPatientData, true); //this header is only set when FALSE, so default to true otherwise

            //if we don't have dates or the extract doesn't contain any patient data, then don't update the audit
            if (extractDate == null
                    || extractCutoff == null
                    || !hasPatientData) {
                return;
            }

            a.setServiceId(serviceId);
            a.setSystemId(systemId);
            a.setSubscriberConfigName(subscriberName);
            a.setSentDate(new Date());
            a.setExchangeId(exchangeId);
            a.setExtractDate(extractDate);
            a.setExtractCutoff(extractCutoff);

            LastDataDalI dal = DalProvider.factoryLastDataDal();
            dal.save(a);


        } catch (Exception ex) {
            throw new PipelineException("Failed to save LastDataToSubscriber audit", ex);
        }
    }
}
