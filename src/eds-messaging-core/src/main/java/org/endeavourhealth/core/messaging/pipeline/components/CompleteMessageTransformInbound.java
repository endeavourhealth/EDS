package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.CompleteMessageTransformInboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CompleteMessageTransformInbound extends PipelineComponent {
    private static final Logger LOG = LoggerFactory.getLogger(CompleteMessageTransformInbound.class);

    private CompleteMessageTransformInboundConfig config;

    public CompleteMessageTransformInbound(CompleteMessageTransformInboundConfig config) {
        this.config = config;
    }

    /**
     * SD-248 the inbound transform (run by MessageTransformInbound) is non-idempotent when it comes
     * to posting messages to RabbitMQ after the transform. If the transform completes OK but then cannot
     * post the results the RabbitMQ protocol queue, it remains in the inbound queue but the audit implies
     * that everything was OK. When the inbound message is processed again, it isn't aware of the previous issue
     * so does not attempt to post the previous messages to the protocol queue. End result is that data doesn't
     * get to subscribers in the event of a RabbitMQ problem.
     *
     * This class solves the above problem by moving the auditing of the transform success to be after the
     * posting to the RabbitMQ protocol queue.
     */
    @Override
    protected void process(Exchange exchange) throws PipelineException {

        try {
            markTransformAuditAsComplete(exchange);

            //SD-517 - delete the exchange message from the temp message queue now complete
            //QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
            //queuedMessageDal.delete(exchange.getId());

        } catch (Exception ex) {
            throw new PipelineException("", ex);
        }
    }

    private void markTransformAuditAsComplete(Exchange exchange) throws Exception {

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

        //find the latest audit of the transform
        ExchangeTransformAudit latest = exchangeDal.getLatestExchangeTransformAudit(exchange.getServiceId(), exchange.getSystemId(), exchange.getId());
        if (latest == null) {
            throw new Exception("Failed to find transform audit for exchange " + exchange.getId());
        }

        if (latest.getEnded() != null) {
            //LOG.debug("Last transform audit is already ended");
            return;
        }

        //LOG.debug("Set last transform audit to ended");
        //actually set the end timestamp
        latest.setEnded(new Date());

        //and save
        exchangeDal.save(latest);
    }
}
