package org.endeavourhealth.core.queueing;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SubscriberQueueHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberQueueHelper.class);

    public static void queueUpFullServiceForSubscriber(UUID serviceId, UUID specificProtocolId) throws Exception {

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getById(serviceId);

        LibraryItem protocol = LibraryRepositoryHelper.getLibraryItem(specificProtocolId);

        LOG.info("Populating subscriber for " + service.getName() + " " + service.getLocalId() + " and protocol " + protocol.getName());

        //find all patients
        LOG.info("Looking for patients at " + serviceId);
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientUuids = patientSearchDal.getPatientIds(serviceId);
        LOG.info("Found " + patientUuids.size() + " for service " + serviceId);

        //create a new "dummy" exchange which we need to get anything sent through the pipeline
        String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
        //String odsCode = service.getLocalId();

        /*Map<UUID, String> orgs = service.getOrganisations();
        if (orgs.size() != 1) {
            throw new Exception("Can't support multiple orgs");
        }
        Iterator<UUID> orgIdIterator = orgs.keySet().iterator();
        UUID orgId = orgIdIterator.next();*/

        /*String endpointJson = service.getEndpoints();
        List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(endpointJson, new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
        //if the service has multiple systems, it doesn't really matter which one we select since we're doing all data for all patients at the service
        JsonServiceInterfaceEndpoint endpoint = endpoints.get(0);
        UUID systemId = endpoint.getSystemUuid();*/

        String[] specificProtocolArr = new String[]{specificProtocolId.toString()};
        String specificProtocolJson = ObjectMapperPool.getInstance().writeValueAsString(specificProtocolArr);

        Exchange exchange = new Exchange();
        exchange.setId(UUID.randomUUID());
        exchange.setBody(bodyJson);
        exchange.setTimestamp(new Date());
        exchange.setHeaders(new HashMap<>());
        exchange.setHeader(HeaderKeys.SenderServiceUuid, serviceId.toString());
        exchange.setHeader(HeaderKeys.IsForPopulatingSubscriber, Boolean.TRUE.toString()); //this tells the outbound transform to do ALL data for each patient
        exchange.setHeader(HeaderKeys.ProtocolIds, specificProtocolJson);
        //exchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
        //exchange.setHeader(HeaderKeys.SenderOrganisationUuid, orgId.toString());
        //exchange.setHeader(HeaderKeys.SenderSystemUuid, systemId.toString());

        exchange.setServiceId(service.getId());
        //exchange.setSystemId(systemId);

        LOG.info("Saving exchange");
        AuditWriter.writeExchange(exchange);
        AuditWriter.writeExchangeEvent(exchange, "Created exchange to populate subscribers in protocol " + protocol.getName());

        LOG.info("Creating exchange batches for " + patientUuids.size() + " patients");
        createExchangeBatches(exchange, patientUuids);

        //post to InboundQueue
        LOG.info("Posting to protocol queue");
        List<UUID> exchangeIds = new ArrayList<>();
        exchangeIds.add(exchange.getId());
        QueueHelper.postToExchange(exchangeIds, "EdsProtocol", specificProtocolId, false);
        LOG.info("Exchange posted to protocol queue");
    }

    private static void createExchangeBatches(Exchange exchange, List<UUID> patientUuids) throws Exception {

        ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
        List<ExchangeBatch> batches = new ArrayList<>();

        //create an admin batch
        batches.add(createBatch(exchange, null));

        for (UUID patientId: patientUuids) {
            batches.add(createBatch(exchange, patientId));

            if (batches.size() >= TransformConfig.instance().getResourceSaveBatchSize()) {
                exchangeBatchDal.save(batches);
                batches.clear();
            }
        }

        if (!batches.isEmpty()) {
            exchangeBatchDal.save(batches);
            batches.clear();
        }
    }

    private static ExchangeBatch createBatch(Exchange exchange, UUID patientId) {
        ExchangeBatch b = new ExchangeBatch();
        b.setExchangeId(exchange.getId());
        b.setNeedsSaving(true);
        b.setBatchId(UUID.randomUUID());
        b.setInsertedAt(new Date());
        b.setEdsPatientId(patientId);

        return b;
    }


}
