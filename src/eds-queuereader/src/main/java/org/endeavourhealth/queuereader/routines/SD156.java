package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
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
import java.util.regex.Pattern;

public class SD156 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD156.class);


    /**
     * updates the audit.service_subscriber_audit table with the content generated in the
     * tmp.SD156_subscriber_start_date table by the other fn in this class. Also removes unnecessary
     * duplicates from that table where we had a bug that caused repeated duplicate records in a
     * handful of cases
     */
    public static void fixAuditStartDateForEachSubscriber(String orgOdsCodeRegex) {
        LOG.info("Fixing audit.service_subscriber_audit for " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            Connection connection = ConnectionManager.getAuditNonPooledConnection();

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                List<Date> dates = new ArrayList<>();
                Map<Date, Set<String>> hmSubscribersByDate = new HashMap<>();
                List<Date> datesToDelete = new ArrayList<>();

                String sql = "SELECT dt_changed, subscriber_config_names"
                            + " FROM service_subscriber_audit"
                            + " WHERE service_id = ?"
                            + " ORDER BY dt_changed";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, service.getId().toString());

                String lastConfigNames = null;

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Date d = new java.util.Date(rs.getTimestamp(1).getTime());
                    String configNames = rs.getString(2);

                    //due to weird behaviour with some List implementations, we have some duplicate empty
                    //records, so detect them and flag them for deletion
                    if (lastConfigNames != null
                            && lastConfigNames.equals("")
                            && configNames.equals(lastConfigNames)) {

                        datesToDelete.add(d);

                    } else {

                        lastConfigNames = configNames;
                        dates.add(d);

                        if (Strings.isNullOrEmpty(configNames)) {
                            hmSubscribersByDate.put(d, new HashSet<>());
                        } else {
                            String[] toks = configNames.split(Pattern.quote("|")); //use the Pattern.quote fn to make it NOT use regex
                            List<String> l = Arrays.asList(toks);
                            hmSubscribersByDate.put(d, new HashSet<>(l));
                        }
                    }
                }
                ps.close();

                List<Date> eventDates = new ArrayList<>();
                List<String> subscribers = new ArrayList<>();
                Map<String, Date> hmSubscriberStart = new HashMap<>();
                Map<String, Date> hmSubscriberEnd = new HashMap<>();

                sql = "SELECT earliest_date, latest_date, subscriber_name"
                        + " FROM tmp.SD156_subscriber_start_date"
                        + " WHERE service_id = ?";
                ps = connection.prepareStatement(sql);
                ps.setString(1, service.getId().toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    Date earliestDate = new java.util.Date(rs.getTimestamp(1).getTime());
                    Date latestDate = new java.util.Date(rs.getTimestamp(2).getTime());
                    String configName = rs.getString(3);

                    if (!eventDates.contains(earliestDate)) {
                        eventDates.add(earliestDate);
                    }
                    if (!eventDates.contains(latestDate)) {
                        eventDates.add(latestDate);
                    }

                    subscribers.add(configName);
                    hmSubscriberStart.put(configName, earliestDate);
                    hmSubscriberEnd.put(configName, latestDate);
                }
                ps.close();

                eventDates.sort(((o1, o2) -> o1.compareTo(o2)));

                Map<Date, Set<String>> hmCalculatedSubscribers = new HashMap<>();

                for (Date eventDate: eventDates) {

                    //work out subscribers effective as of this date
                    Set<String> subscriberSet = new HashSet<>();

                    for (String configName: subscribers) {
                        Date earliestDate = hmSubscriberStart.get(configName);
                        Date latestDate = hmSubscriberEnd.get(configName);

                        if (eventDate.before(earliestDate)) {
                            continue;
                        }
                        if (!eventDate.before(latestDate)) {
                            continue;
                        }

                        subscriberSet.add(configName);
                    }

                    hmCalculatedSubscribers.put(eventDate, subscriberSet);
                }

                //stick all our new data in a table so we can check it over then manually copy
                sql = "INSERT INTO tmp.SD156_service_subscriber_audit_new (service_id, dt_changed, subscriber_config_names)"
                        + " VALUES (?, ?, ?)";
                ps = connection.prepareStatement(sql);

                for (Date eventDate: eventDates) {
                    Set<String> subscriberSet = hmCalculatedSubscribers.get(eventDate);

                    List<String> l = new ArrayList<>(subscriberSet);
                    l.sort((a, b) -> a.compareToIgnoreCase(b));
                    String configNameStr = String.join("|", l);

                    ps.setString(1, service.getId().toString());
                    ps.setTimestamp(2, new java.sql.Timestamp(eventDate.getTime()));
                    ps.setString(3, configNameStr);
                    ps.executeUpdate();
                }
                connection.commit();

                sql = "INSERT INTO tmp.SD156_service_subscriber_audit_delete (service_id, dt_changed)"
                        + " VALUES (?, ?)";
                ps = connection.prepareStatement(sql);

                for (Date d: datesToDelete) {
                    ps.setString(1, service.getId().toString());
                    ps.setTimestamp(2, new java.sql.Timestamp(d.getTime()));
                    ps.executeUpdate();
                }
                connection.commit();
            }

            connection.close();

            LOG.info("Finished Fixing audit.service_subscriber_audit for " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * finds the date that each subscriber feed started for each publisher service
     * this is currently audited in service_subscriber_audit, but we don't have this captured prior
     * to mid-2020, so this routine will back-fill that content
     */
    public static void findStartDateForEachSubscriber(boolean includeStartedButNotFinishedServices, String orgOdsCodeRegex) {
        LOG.info("Finding Exchanges Not Sent To Subscriber for " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            String bulkOperationName = "find start date for each subscriber (SD-156)";

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                if (includeStartedButNotFinishedServices) {
                    //check if already done, so we can make sure EVERY service is done
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }

                } else {
                    //check if already started, to allow us to run multiple instances of this at once
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already started or done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);

                Map<String, Date> hmSubscriberStartDates = new HashMap<>();
                Map<String, Date> hmSubscriberEndDates = new HashMap<>();

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    //get all exchanges
                    List<UUID> exchangeIds = exchangeDal.getExchangeIdsForService(service.getId(), systemId);
                    LOG.debug("Found " + exchangeIds.size() + " exchanges");

                    //SQL to find each subscriber date for an exchange was sent to
                    Connection connection = ConnectionManager.getAuditNonPooledConnection();
                    String sql = "SELECT DISTINCT a.started, a.subscriber_config_name"
                            + " FROM exchange_batch b"
                            + " INNER JOIN exchange_subscriber_transform_audit a"
                            + " ON b.exchange_id = a.exchange_id"
                            + " AND b.batch_id = a.exchange_batch_id"
                            + " WHERE b.exchange_id = ?";
                    PreparedStatement ps = connection.prepareStatement(sql);

                    int done = 0;

                    //going oldest-to-newest
                    for (UUID exchangeId: exchangeIds) {
                        ps.setString(1, exchangeId.toString());
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            int col = 1;
                            Date auditDate = new java.util.Date(rs.getTimestamp(col++).getTime());
                            String subscriberName = rs.getString(col++);

                            Date foundDate = hmSubscriberStartDates.get(subscriberName);
                            if (foundDate == null
                                    || auditDate.before(foundDate)) {
                                hmSubscriberStartDates.put(subscriberName, auditDate);
                            }

                            foundDate = hmSubscriberEndDates.get(subscriberName);
                            if (foundDate == null
                                    || auditDate.after(foundDate)) {
                                hmSubscriberEndDates.put(subscriberName, auditDate);
                            }
                        }

                        done ++;
                        if (done % 100 == 0) {
                            LOG.debug("Done " + done + " / " + exchangeIds.size());
                        }
                    }

                    LOG.debug("Done " + done + " / " + exchangeIds.size());

                    ps.close();
                    connection.close();

                    auditSubscriberStartDates(service.getId(), hmSubscriberStartDates, hmSubscriberEndDates);

                    //audit that we've done
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }
            }

            LOG.info("Finished Finding Exchanges Not Sent To Subscriber for " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void auditSubscriberStartDates(UUID serviceId, Map<String, Date> hmSubscriberStartDates, Map<String, Date> hmSubscriberEndDates) throws Exception {
        if (hmSubscriberStartDates.isEmpty()) {
            return;
        }

        Connection connection = ConnectionManager.getAdminConnection();
        String sql = "REPLACE INTO tmp.SD156_subscriber_start_date (service_id, earliest_date, latest_date, subscriber_name) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        for (String subscriberName: hmSubscriberStartDates.keySet()) {
            Date earliestDate = hmSubscriberStartDates.get(subscriberName);
            Date latestDate = hmSubscriberEndDates.get(subscriberName);

            int col = 1;

            ps.setString(col++, serviceId.toString());
            ps.setTimestamp(col++, new java.sql.Timestamp(earliestDate.getTime()));
            ps.setTimestamp(col++, new java.sql.Timestamp(latestDate.getTime()));
            ps.setString(col++, subscriberName);

            ps.addBatch();
        }

        ps.executeBatch();
        connection.commit();

        ps.close();
        connection.close();
    }


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
    public static void findExchangesNotSentToSubscriber(boolean onlySkipCompletedOnes, String orgOdsCodeRegex) {
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

                //get all subscribers
                List<String> subscribers = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId(), service.getLocalId());
                if (subscribers.isEmpty()) {
                    LOG.debug("No subscribers so skipping " + service);
                    continue;
                }

                if (onlySkipCompletedOnes) {
                    //check if already done, so we can make sure EVERY service is done
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }

                } else {
                    //check if already started, to allow us to run multiple instances of this at once
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already started or done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
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
