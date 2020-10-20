package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SD156 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD156.class);


    /**
     * see https://endeavourhealth.atlassian.net/browse/SD-156
     *
     create table tmp.SD156_batches_not_transformed (
     service_id char(36),
     system_id char(36),
     batch_id char(36),
     patient_id char(36),
     inserted_at datetime(3),
     subscriber_name varchar(255),
     is_ok boolean,
     ok_reason varchar(255)
     );
     */
    public static void findExchangesNotSentToSubscriber(String orgOdsCodeRegex) {
        LOG.info("Finding Exchanges Not Sent To Subscriber for " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            String bulkOperationName = "find batches not sent to subscriber SD-156";

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                //check if already done
                //check if already started, to allow us to run multiple instances of this at once
                //if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);

                //get all subscribers
                List<String> subscribers = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId(), service.getLocalId());
                if (subscribers.isEmpty()) {
                    LOG.debug("No subscribers so skipping");
                    continue;
                }

                LOG.debug("Got expected subscribers " + subscribers);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    //get all exchanges
                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");

                    Connection connection = ConnectionManager.getAuditNonPooledConnection();
                    String sql = "SELECT DISTINCT b.batch_id, b.eds_patient_id, b.inserted_at, a.subscriber_config_name"
                            + " FROM exchange_batch b"
                            + " LEFT OUTER JOIN exchange_subscriber_transform_audit a"
                            + " ON b.exchange_id = a.exchange_id"
                            + " AND b.batch_id = a.exchange_batch_id"
                            + " WHERE b.exchange_id = ?"
                            + " ORDER BY b.batch_id";
                    PreparedStatement ps = connection.prepareStatement(sql);

                    int done = 0;

                    for (Exchange exchange: exchanges) {

                        UUID exchangeId = exchange.getId();
                        ps.setString(1, exchangeId.toString());
                        ResultSet rs = ps.executeQuery();


                        UUID lastBatchId = null;
                        String lastPatientId = null;
                        Date lastInsertedAt = null;
                        List<String> subscribersFound = null;

                        while (rs.next()) {
                            int col = 1;
                            UUID batchId = UUID.fromString(rs.getString(col++));
                            String patientId = rs.getString(col++);
                            Date insertedAt = new java.util.Date(rs.getTimestamp(col++).getTime());
                            String subscriberName = rs.getString(col++);

                            if (subscriberName == null) {
                                //if a null subscriber, then it wasn't sent to ANY subscriber
                                auditMissedTransform(service.getId(), systemId, batchId, patientId, insertedAt, new ArrayList(), subscribers);
                                lastBatchId = null;
                                lastPatientId = null;
                                lastInsertedAt = null;
                                subscribersFound = null;

                            } else if (lastBatchId != null && lastBatchId.equals(batchId)) {
                                //if the same batch as last time, just add the new subscriber
                                subscribersFound.add(subscriberName);

                            } else {
                                //if the first or a different batch
                                if (lastBatchId != null) {
                                    auditMissedTransform(service.getId(), systemId, lastBatchId, lastPatientId, lastInsertedAt, subscribersFound, subscribers);
                                }

                                lastBatchId = batchId;
                                lastPatientId = patientId;
                                lastInsertedAt = insertedAt;
                                subscribersFound = new ArrayList<>();

                                subscribersFound.add(subscriberName);
                            }
                        }

                        //don't forget the last batch
                        if (lastBatchId != null) {
                            auditMissedTransform(service.getId(), systemId, lastBatchId, lastPatientId, lastInsertedAt, subscribersFound, subscribers);
                        }

                        done ++;
                        if (done % 100 == 0) {
                            LOG.debug("Done " + done + " / " + exchanges.size());
                        }
                    }

                    LOG.debug("Done " + done + " / " + exchanges.size());

                    ps.close();
                    connection.close();

                    //audit that we've done
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }
            }

            LOG.info("Finished Finding Exchanges Not Sent To Subscriber for " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void auditMissedTransform(UUID serviceId, UUID systemId, UUID lastBatchId,
                                             String lastPatientId, Date lastInsertedAt, List<String> subscribersFound, List<String> subscribers) throws Exception {
        Set<String> missingSubscribers = new HashSet<>(subscribers);
        missingSubscribers.removeAll(subscribersFound);

        if (missingSubscribers.isEmpty()) {
            return;
        }

        //LOG.debug("Batch " + lastBatchId + " didn't go to subscribers " + missingSubscribers);

        Connection connection = ConnectionManager.getAdminConnection();
        String sql = "INSERT INTO tmp.SD156_batches_not_transformed (service_id, system_id, batch_id, patient_id, inserted_at, subscriber_name) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        for (String missingSubscriber: missingSubscribers) {
            int col = 1;

            ps.setString(col++, serviceId.toString());
            ps.setString(col++, systemId.toString());
            ps.setString(col++, lastBatchId.toString());
            ps.setString(col++, lastPatientId);
            ps.setTimestamp(col++, new java.sql.Timestamp(lastInsertedAt.getTime()));
            ps.setString(col++, missingSubscriber);

            ps.addBatch();
        }

        ps.executeBatch();
        connection.commit();

        ps.close();
        connection.close();
    }
}
