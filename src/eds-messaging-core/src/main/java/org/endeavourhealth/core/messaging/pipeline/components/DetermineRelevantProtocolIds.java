package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeProtocolErrorDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.subscribers.PublisherHelper;
import org.endeavourhealth.transform.common.AuditWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DetermineRelevantProtocolIds extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;
	private static ExchangeProtocolErrorDalI errorDal = DalProvider.factoryExchangeProtocolErrorDal();

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		try {

			UUID exchangeId = exchange.getId();
			UUID serviceUuid = exchange.getServiceId();
			String odsCode = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
			boolean hasDpa = PublisherHelper.hasDpa(exchangeId, serviceUuid, odsCode);

			if (!hasDpa) {
				errorDal.save(exchange.getId());
				throw new PipelineException("No DPA found for service " + serviceUuid);
			}

			AuditWriter.writeExchange(exchange);

		} catch (PipelineException pe) {
			//if we get a pipeline exception, just throw as is
			throw pe;

		} catch (Exception ex) {
			//if we get any other type of exception, it needs to be re-packaged
			throw new PipelineException("Error processing exchange " + exchange.getId(), ex);
		}
	}

}
