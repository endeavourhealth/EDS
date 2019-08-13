package org.endeavourhealth.core.queueing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.messaging.pipeline.components.DetermineRelevantProtocolIds;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.messaging.pipeline.components.RunDataDistributionProtocols;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.*;
import java.util.stream.Collectors;

public class QueueHelper {
    private static final Logger LOG = LoggerFactory.getLogger(QueueHelper.class);


    public static void postToExchange(List<UUID> exchangeIds, String exchangeName, UUID specificProtocolId, boolean recalculateProtocols) throws Exception {
        postToExchange(exchangeIds, exchangeName, specificProtocolId, recalculateProtocols, null);
    }

    public static void postToExchange(List<UUID> exchangeIds, String exchangeName, UUID specificProtocolId, boolean recalculateProtocols, Set<String> fileTypesToFilterOn) throws Exception {

        PostMessageToExchangeConfig exchangeConfig = findExchangeConfig(exchangeName);
        if (exchangeConfig == null) {
            throw new BadRequestException("Failed to find PostMessageToExchange config details for exchange " + exchangeName);
        }

        if (exchangeIds.isEmpty()) {
            return;
        }

        String newProtocolIdsJson = null;
        if (recalculateProtocols) {
            UUID firstExchangeId = exchangeIds.get(0);
            Exchange firstExchange = AuditWriter.readExchange(firstExchangeId);
            String serviceUuid = firstExchange.getHeader(HeaderKeys.SenderServiceUuid);
            String systemUuid = firstExchange.getHeader(HeaderKeys.SenderSystemUuid);

            newProtocolIdsJson = DetermineRelevantProtocolIds.getProtocolIdsForPublisherService(serviceUuid, systemUuid, firstExchangeId);
        }

        for (int i=0; i<exchangeIds.size(); i++) {

            UUID exchangeId = exchangeIds.get(i);
            Exchange exchange = AuditWriter.readExchange(exchangeId);
            //org.endeavourhealth.core.messaging.exchange.Exchange exchange = retrieveExchange(exchangeId);

            //to make sure the latest setup applies, re-calculate the protocols that apply to this exchange
            if (recalculateProtocols) {
                String oldProtocolIdsJson = exchange.getHeader(HeaderKeys.ProtocolIds);
                if (!newProtocolIdsJson.equals(oldProtocolIdsJson)) {
                    exchange.setHeader(HeaderKeys.ProtocolIds, newProtocolIdsJson);
                    AuditWriter.writeExchange(exchange);
                }
            }

            String[] protocolIds = null;
            try {
                protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);
            } catch (Exception ex) {
                //if an exchange has no valid protocols, then don't let it go into the queue.
                //If a publishing service isn't in a protocol, then whenever the SFTP Reader tries to post data for it,
                //it'll still create the exchange, but without any protocols. So we don't want these exchanges
                //then accidentally going into the inbound queues.
                LOG.debug("Skipping exchange " + exchangeId + " as it has no publisher protocol ID");
                continue;
            }

            //if we want to restrict the protocols applied (e.g. only want to populate a specific subscriber)
            //then filter the protocols in the header
            if (specificProtocolId != null) {

                //validate the selected protocol does apply to this exchange
                boolean foundInHeader = false;
                for (String protocolId : protocolIds) {
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

            String exchangeEventStr = "Manually pushed into " + exchangeName + " exchange";

            //apply any filtering on file type
            if (fileTypesToFilterOn != null) {
                List<ExchangePayloadFile> filteredFiles = new ArrayList<>();

                List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody(), false);
                for (ExchangePayloadFile file: files) {
                    String fileType = file.getType();
                    if (fileTypesToFilterOn.contains(fileType)) {
                        filteredFiles.add(file);
                    }
                }

                String newBody = JsonSerializer.serialize(filteredFiles);
                exchange.setBody(newBody);

                List<String> list = new ArrayList<>(fileTypesToFilterOn);
                list.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));
                exchangeEventStr += "\nFiltered on file types:\n" + String.join("\n", list);
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
            AuditWriter.writeExchangeEvent(exchange, exchangeEventStr);

            //if pushed into the Inbound queue, and it previously had an error in there, mark it as resubmitted
            //LOG.trace("Posting " + exchange.getId() + " into exchange [" + exchangeName + "] for service " + exchange.getServiceId() + " and system " + exchange.getSystemId());
            if (exchangeName.equalsIgnoreCase("EdsInbound")) { //difference case used on different servers

                UUID serviceId = exchange.getServiceId();
                UUID systemId = exchange.getSystemId();

                ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
                ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
                //LOG.trace("Is inbound exchange and found audit " + audit);
                if (audit != null && !audit.isResubmitted()) {
                    audit.setResubmitted(true);
                    auditRepository.save(audit);
                    //LOG.trace("Audit set to resubmitted = true");
                }
            }

            if (i % 1000 == 0) {
                LOG.info("Posted " + (i+1) + " / " + exchangeIds.size() + " exchanges to " + exchangeName);
            }
        }


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
            UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);

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
                    Map<ResourceType, List<UUID>> filteredResources = RunDataDistributionProtocols.filterResources(serviceId, libraryItem.getProtocol(), batchId);
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

    public static PostMessageToExchangeConfig findExchangeConfig(String exchangeName) throws Exception {
        //LOG.debug("Looking for config for exchange [" + exchangeName + "]");

        //go through all the known app configs to find config for posting to the Rabbit Exchange we're interested in
        String messagingApiConfigXml = ConfigManager.getConfiguration("api-configuration", "messaging-api");
        PostMessageToExchangeConfig config = findExchangeConfig(messagingApiConfigXml, exchangeName);
        if (config != null) {
            //LOG.debug("Found in messaging API XML");
            return config;
        }

        Map<String, String> queueReadConfigs = ConfigManager.getConfigurations("queuereader");
        for (String configId: queueReadConfigs.keySet()) {
            //LOG.debug("Checking config XML for " + configId + " for exchangeName = "+exchangeName);
            String queueReaderConfigXml = queueReadConfigs.get(configId);

            //the transform common config record is JSON, and trying to parse JSON as XML results
            //in confusing logging to console (which seems impossible to turn off),
            //saying "[Fatal Error] :1:1: Content is not allowed in prolog"
            //So avoid confusion by not even trying with them
            if (queueReaderConfigXml.startsWith("{")) {
                continue;
            }

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
            //LOG.debug("Deserialised as messaging API XML");
            ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();
            pipeline = postConfig.getPipeline();

        } catch (Exception ex) {

            try {
                QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
                //LOG.debug("Deserialised as Queue Reader XML");
                pipeline = configuration.getPipeline();

            } catch (Exception ex2) {
                //we also have some non-XML config records in there, so we'll get another exception for them
                return null;
            }
        }
        //LOG.debug("Got pipeline " + pipeline.getPipelineComponents());

        for (ComponentConfig comp: pipeline.getPipelineComponents()) {
            //LOG.debug("Got component " + comp.getClass().getName());
            if (comp instanceof PostMessageToExchangeConfig) {
                PostMessageToExchangeConfig exchangeConfig = (PostMessageToExchangeConfig)comp;
                //LOG.debug("Config exchange name = [" + exchangeConfig.getExchange() + "]");
                if (exchangeConfig.getExchange().equalsIgnoreCase(exchangeName)) {
                    //LOG.debug("Found match!");
                    return exchangeConfig;
                }
            }
        }

        return null;

        /*return pipeline
                .getPipelineComponents()
                .stream()
                .filter(t -> t instanceof PostMessageToExchangeConfig)
                .map(t -> (PostMessageToExchangeConfig)t)
                .filter(t -> t.getExchange().equalsIgnoreCase(exchangeName))
                .collect(StreamExtension.singleOrNullCollector());*/
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
