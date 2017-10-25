package org.endeavourhealth.core.queueing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.messaging.pipeline.components.DetermineRelevantProtocolIds;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.messaging.pipeline.components.RunDataDistributionProtocols;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueueHelper {
    private static final Logger LOG = LoggerFactory.getLogger(QueueHelper.class);

    public static void postToExchange(UUID exchangeId, String exchangeName, UUID specificProtocolId, boolean recalculateProtocols) throws Exception {

        PostMessageToExchangeConfig exchangeConfig = findExchangeConfig(exchangeName);
        if (exchangeConfig == null) {
            throw new BadRequestException("Failed to find PostMessageToExchange config details for exchange " + exchangeName);
        }

        Exchange exchange = AuditWriter.readExchange(exchangeId);
        //org.endeavourhealth.core.messaging.exchange.Exchange exchange = retrieveExchange(exchangeId);

        //to make sure the latest setup applies, re-calculate the protocols that apply to this exchange
        if (recalculateProtocols) {
            String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
            String newProtocolIdsJson = DetermineRelevantProtocolIds.getProtocolIdsForPublisherService(serviceUuid);
            String oldProtocolIdsJson = exchange.getHeader(HeaderKeys.ProtocolIds);
            if (!newProtocolIdsJson.equals(oldProtocolIdsJson)) {
                exchange.setHeader(HeaderKeys.ProtocolIds, newProtocolIdsJson);
                AuditWriter.writeExchange(exchange);
            }
        }

        //if we want to restrict the protocols applied (e.g. only want to populate a specific subscriber)
        //then filter the protocols in the header
        if (specificProtocolId != null) {

            //validate the selected protocol does apply to this exchange
            boolean foundInHeader = false;
            String[] protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
            for (String protocolId: protocolIds) {
                if (protocolId.equals(specificProtocolId.toString())) {
                    foundInHeader = true;
                    break;
                }
            }

            if (!foundInHeader) {
                throw new BadRequestException("Restricting to protocol " + specificProtocolId + " but that doesn't apply to exchange " + exchangeId);
            }

            String[] specificProtocolArr = new String[]{specificProtocolId.toString()};
            String specificProtocolJson = ObjectMapperPool.getInstance().writeValueAsString(specificProtocolArr);
            exchange.setHeader(HeaderKeys.ProtocolIds, specificProtocolJson);
        }

        //work out what multicast header we need
        String multicastHeader = exchangeConfig.getMulticastHeader();
        if (!Strings.isNullOrEmpty(multicastHeader)) {
            if (exchange.getHeader(multicastHeader) == null) {
                populateMulticastHeader(exchange, multicastHeader);
            }
        }

        //re-post back into Rabbit using the same pipeline component as used by the messaging API
        PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
        component.process(exchange);

        //write an event for the exchange, so we can see this happened
        AuditWriter.writeExchangeEvent(exchange, "Manually pushed into " + exchangeName + " exchange");

        //if pushed into the Inbound queue, and it previously had an error in there, mark it as resubmitted
        /*if (exchangeName.equals("EdsInbound")) {

            UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
            UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SystemVersion);

            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            if (!audit.isResubmitted()) {
                audit.setResubmitted(true);
                auditRepository.save(audit);
            }
        }*/
    }


    private static void populateMulticastHeader(Exchange exchange, String multicastHeader) throws Exception {

        UUID exchangeUuid = exchange.getId();
        ExchangeBatchDalI exchangeBatchRepository = DalProvider.factoryExchangeBatchDal();
        List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);

        if (multicastHeader.equalsIgnoreCase(HeaderKeys.BatchIdsJson)) {

            List<UUID> batchUuids = batches
                    .stream()
                    .map(t -> t.getBatchId())
                    .collect(Collectors.toList());
            try {
                String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
                exchange.setHeader(HeaderKeys.BatchIdsJson, batchUuidsStr);

            } catch (JsonProcessingException e) {
                LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
            }

        } else if (multicastHeader.equalsIgnoreCase(HeaderKeys.TransformBatch)) {

            List<TransformBatch> transformBatches = new ArrayList<>();

            String[] protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);

            for (String protocolId: protocolIds) {

                UUID protocolUuid = UUID.fromString(protocolId);
                LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItem(protocolUuid);
                List<ServiceContract> subscribers = libraryItem
                        .getProtocol()
                        .getServiceContract()
                        .stream()
                        .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                        .collect(Collectors.toList());

                //if there's no subscribers on this protocol, just skip it
                if (subscribers.isEmpty()) {
                    continue;
                }

                for (ExchangeBatch batch: batches) {

                    String batchId = batch.getBatchId().toString();
                    Map<ResourceType, List<UUID>> filteredResources = RunDataDistributionProtocols.filterResources(libraryItem.getProtocol(), batchId);
                    if (filteredResources.isEmpty()) {
                        continue;
                    }

                    //the only bit of data we can populate is the batch ID
                    TransformBatch transformBatch = new TransformBatch();
                    transformBatch.setBatchId(batch.getBatchId());
                    transformBatch.setProtocolId(UUID.fromString(protocolId));
                    transformBatch.setSubscribers(subscribers);
                    transformBatch.setResourceIds(filteredResources);

                    transformBatches.add(transformBatch);
                }
            }

            try {
                String transformBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(transformBatches);
                exchange.setHeader(HeaderKeys.TransformBatch, transformBatchesJson);

            } catch (JsonProcessingException e) {
                LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
            }

        } else if (multicastHeader.equalsIgnoreCase(HeaderKeys.SubscriberBatch)) {

            throw new BadRequestException("Mutlicasting using subscriber batch not supported");

        } else {
            throw new BadRequestException("Unsupported multicast header: " + multicastHeader);
        }

    }

    private static PostMessageToExchangeConfig findExchangeConfig(String exchangeName) throws Exception {

        //go through all the known app configs to find config for posting to the Rabbit Exchange we're interested in
        String messagingApiConfigXml = ConfigManager.getConfiguration("api-configuration", "messaging-api");
        PostMessageToExchangeConfig config = findExchangeConfig(messagingApiConfigXml, exchangeName);
        if (config != null) {
            return config;
        }

        Map<String, String> queueReadConfigs = ConfigManager.getConfigurations("queuereader");
        for (String configId: queueReadConfigs.keySet()) {
            String queueReaderConfigXml = queueReadConfigs.get(configId);

            config = findExchangeConfig(queueReaderConfigXml, exchangeName);
            if (config != null) {
                return config;
            }
        }

        return null;
    }

    private static PostMessageToExchangeConfig findExchangeConfig(String configXml, String exchangeName) throws Exception {

        Pipeline pipeline = null;

        //the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
        try {
            ApiConfiguration config = ConfigWrapper.deserialise(configXml);
            ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();
            pipeline = postConfig.getPipeline();

        } catch (Exception e) {

            QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
            pipeline = configuration.getPipeline();
        }

        return pipeline
                .getPipelineComponents()
                .stream()
                .filter(t -> t instanceof PostMessageToExchangeConfig)
                .map(t -> (PostMessageToExchangeConfig)t)
                .filter(t -> t.getExchange().equalsIgnoreCase(exchangeName))
                .collect(StreamExtension.singleOrNullCollector());
    }

    /*private static PostMessageToExchangeConfig findExchangeConfig(String exchangeName) throws Exception {

        //go through all the known app configs to find config for posting to the Rabbit Exchange we're interested in
        PostMessageToExchangeConfig config = findExchangeConfig("messaging-api", "api-configuration", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "inbound", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "protocol", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "response", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "subscriber", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "transform", exchangeName);
        if (config != null) {
            return config;
        }

        return null;
    }


    private static PostMessageToExchangeConfig findExchangeConfig(String appId, String configId, String exchangeName) throws Exception {

        //we access the messaging API config directly, to find out how it posts new incoming exchanges to rabbit
        String configXml = ConfigManager.getConfiguration(configId, appId);

        Pipeline pipeline = null;

        //the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
        try {
            ApiConfiguration config = ConfigWrapper.deserialise(configXml);
            ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();
            pipeline = postConfig.getPipeline();

        } catch (Exception e) {

            QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
            pipeline = configuration.getPipeline();
        }

        return pipeline
                .getPipelineComponents()
                .stream()
                .filter(t -> t instanceof PostMessageToExchangeConfig)
                .map(t -> (PostMessageToExchangeConfig)t)
                .filter(t -> t.getExchange().equalsIgnoreCase(exchangeName))
                .collect(StreamExtension.singleOrNullCollector());
    }*/
}
