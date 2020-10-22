package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;

public class Queuing extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(Queuing.class);


    public static void postToRabbit(String exchangeName, String srcFile, String reason, Integer throttle) {
        LOG.info("Posting to " + exchangeName + " from " + srcFile);
        if (throttle != null) {
            LOG.info("Throttled to " + throttle + " messages/second");
        }

        try {

            QueueHelper.ExchangeName xName = QueueHelper.ExchangeName.fromName(exchangeName);

            File src = new File(srcFile);

            //create file of ones done
            File dir = src.getParentFile();
            String name = "DONE" + src.getName();
            File dst = new File(dir, name);

            Set<UUID> hsAlreadyDone = new HashSet<>();
            if (dst.exists()) {
                List<String> lines = Files.readAllLines(dst.toPath());
                for (String line : lines) {
                    if (!Strings.isNullOrEmpty(line)) {
                        try {
                            UUID uuid = UUID.fromString(line);
                            hsAlreadyDone.add(uuid);
                        } catch (Exception ex) {
                            LOG.error("Skipping line " + line);
                        }
                    }
                }

                LOG.info("Already done " + hsAlreadyDone.size());
            }

            List<UUID> exchangeIds = new ArrayList<>();
            int countTotal = 0;

            List<String> lines = Files.readAllLines(src.toPath());
            for (String line : lines) {
                if (!Strings.isNullOrEmpty(line)) {
                    try {
                        UUID uuid = UUID.fromString(line);
                        countTotal++;

                        if (!hsAlreadyDone.contains(uuid)) {
                            exchangeIds.add(uuid);
                        }

                    } catch (Exception ex) {
                        LOG.error("Skipping line " + line);
                    }
                }
            }
            LOG.info("Found " + countTotal + " down to " + exchangeIds.size() + " skipping ones already done, to post to " + exchangeName);

            continueOrQuit();

            FileWriter fileWriter = new FileWriter(dst, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            long startMs = System.currentTimeMillis();
            int doneThisSecond = 0;

            LOG.info("Posting " + exchangeIds.size() + " to " + exchangeName);
            for (int i = 0; i < exchangeIds.size(); i++) {

                UUID exchangeId = exchangeIds.get(i);
                List<UUID> tmp = new ArrayList<>();
                tmp.add(exchangeId);
                QueueHelper.postToExchange(tmp, xName, null, reason);

                printWriter.println(exchangeId.toString());
                printWriter.flush();

                if (i % 5000 == 0) {
                    LOG.debug("Done " + i + " / " + exchangeIds.size());
                }

                if (throttle != null) {
                    doneThisSecond++;

                    if (doneThisSecond > throttle.intValue()) {
                        long now = System.currentTimeMillis();
                        long sleep = 1000 - (now - startMs);

                        if (sleep > 0) {
                            Thread.sleep(sleep);
                        }

                        startMs = System.currentTimeMillis();
                        doneThisSecond = 0;
                    }
                }
            }

            printWriter.close();

            LOG.info("Finished Posting to " + exchangeName + " from " + srcFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    public static void postExchangesToProtocol(String srcFile, String reason) {
        LOG.info("Posting to protocol from " + srcFile);
        try {
            List<UUID> exchangeIds = new ArrayList<>();

            List<String> lines = Files.readAllLines(new File(srcFile).toPath());
            for (String line: lines) {
                if (!Strings.isNullOrEmpty(line)) {
                    UUID uuid = UUID.fromString(line);
                    exchangeIds.add(uuid);
                }
            }

            LOG.info("Posting " + exchangeIds.size() + " to Protocol queue");
            QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, reason);

            LOG.info("Finished Posting to protocol from " + srcFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void postPatientsToProtocol(UUID serviceId, UUID systemId, String sourceFile) {

        try {
            LOG.info("Posting patient from " + sourceFile + " for " + serviceId + " to Protocol queue");
            Set<UUID> hsPatientUuids = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(sourceFile).toPath());
            for (String line: lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
            LOG.info("Found " + hsPatientUuids.size() + " patient IDs");

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getById(serviceId);
            LOG.info("Service " + service.getId() + " -> " + service.getName());

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
            LOG.info("Found " + exchanges.size() + " exchanges");

            ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();

            //exchanges are in order most recent first, so iterate backwards to get them in date order
            for (int i=exchanges.size()-1; i>=0; i--) {
                Exchange exchange = exchanges.get(i);

                List<UUID> batchesForPatient = new ArrayList<>();

                List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchange.getId());
                for (ExchangeBatch batch: batches) {
                    UUID patientId = batch.getEdsPatientId();
                    if (patientId != null
                            && hsPatientUuids.contains(patientId)) {

                        batchesForPatient.add(batch.getBatchId());
                    }
                }

                if (!batchesForPatient.isEmpty()) {
                    LOG.debug("Posting " + batchesForPatient.size() + " for exchange " + exchange.getId() + " to rabbit");

                    //set new batch ID in exchange header
                    String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchesForPatient.toArray());
                    exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                    //post new batch to protocol Q
                    PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig(QueueHelper.ExchangeName.PROTOCOL);
                    PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
                    component.process(exchange);
                }
            }

            LOG.info("Finished posting patients from " + sourceFile + " for " + serviceId + " to Protocol queue");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }
}
