package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.publisherCommon.TppStaffDalI;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.tpp.csv.helpers.TppCsvHelper;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD86 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD86.class);

    /**
     * tests time taken to do lookups
     */
    public static void testLookupTiming() {
        LOG.debug("Testing Lookup Timing");
        try {
            UUID serviceId = UUID.fromString("ccd1a468-12bd-4407-b9ad-33f3547f16ec");
            Random r = new Random(System.currentTimeMillis());

            long msStart = System.currentTimeMillis();
            LOG.debug("Testing profile ID lookups");
            for (int i=0; i<120000; i++) {

                //look up ID mapping for profile ID
                Set<Reference> refs = new HashSet<>();
                refs.add(ReferenceHelper.createReference(ResourceType.Practitioner, "" + r.nextInt()));
                Map<Reference, UUID> mappings = IdHelper.getEdsResourceIds(serviceId, refs);
                if (i % 1000 == 0) {
                    LOG.debug("DOne " + i);
                }
            }
            long msEnd = System.currentTimeMillis();
            LOG.debug("Finished profile ID lookups at " + (msEnd - msStart) + "ms");


            msStart = System.currentTimeMillis();
            LOG.debug("Testing profile ID lookups");
            for (int i=0; i<120000; i++) {

                //look up profile ID for staff ID
                TppStaffDalI dal = DalProvider.factoryTppStaffMemberDal();
                Set<Integer> staffIds = new HashSet<>();
                staffIds.add(new Integer(r.nextInt()));
                Map<Integer, Integer> hmStaffAndProfileIds = dal.findProfileIdsForStaffMemberIdsAtOrg("" + r.nextInt(), staffIds);

                //look up ID mapping for profile ID
                Set<Reference> refs = new HashSet<>();
                refs.add(ReferenceHelper.createReference(ResourceType.Practitioner, "" + r.nextInt()));
                Map<Reference, UUID> mappings = IdHelper.getEdsResourceIds(serviceId, refs);
                if (i % 1000 == 0) {
                    LOG.debug("DOne " + i);
                }
            }
            msEnd = System.currentTimeMillis();
            LOG.debug("Finished staff ID lookups at " + (msEnd - msStart) + "ms");


            LOG.debug("Finished Testing Lookup Timing");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * routine to fix SD-86
     */
    public static void fixTppMissingPractitioners(String orgOdsCodeRegex) {
        LOG.debug("Fixing missing TPP practitioner at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();


            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "fix null TPP practitioners SD-86";

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                //check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);

                UUID serviceId = service.getId();
                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("" + systemIds.size() + " system IDs found");
                }
                UUID systemId = systemIds.get(0);

                Set<Long> hsImmunisationDone = new HashSet<>();

                List<UUID> batchIdsCreated = new ArrayList<>();

                FhirResourceFiler filer = null;
                Exchange newExchange = null;

                UUID exchangeId = UUID.randomUUID();
                String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                String odsCode = service.getLocalId();

                filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                newExchange = new Exchange();
                newExchange.setId(exchangeId);
                newExchange.setBody(bodyJson);
                newExchange.setTimestamp(new Date());
                newExchange.setHeaders(new HashMap<>());
                newExchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
                newExchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
                newExchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
                newExchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
                newExchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.TPP_CSV);
                newExchange.setServiceId(service.getId());
                newExchange.setSystemId(systemId);

                AuditWriter.writeExchange(newExchange);
                AuditWriter.writeExchangeEvent(newExchange, "Manually created to correct null TPP practitioners (SD-86)");

                List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
                LOG.debug("Found " + exchanges.size() + " exchanges");

                TppCsvHelper csvHelper = new TppCsvHelper(serviceId, systemId, exchangeId);

                try {

                    /**
                     SRDrugSensitivity
                     SREvent
                     SRImmunisation
                     SRPrimaryCareMedication
                     SRRecall
                     SRReferralOut
                     SRRepeatTemplate
                     */

                    doDrugSensitivty(exchanges, csvHelper, filer);
/*

                    //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
                    for (int i=0; i<exchanges.size(); i++) {
                        Exchange exchange = exchanges.get(i);

                        String exchangeBody = exchange.getBody();
                        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

                        for (ExchangePayloadFile file: files) {
                            if (file.getType().equals("Immunisation")) {

                                String path = file.getPath();
                                InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
                                CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                                Iterator<CSVRecord> iterator = parser.iterator();

                                while (iterator.hasNext()) {
                                    CSVRecord record = iterator.next();

                                    String recordIdStr = record.get("RowIdentifier");
                                    Long recordId = Long.valueOf(recordIdStr);
                                    if (!hsImmunisationDone.contains(recordId)) {

                                        String doneBy = record.get("IDDoneBy");
                                        String doneAt = record.get("IDOrganisationDoneAt");

                                        if (!Strings.isNullOrEmpty(doneAt)
                                                && (Strings.isNullOrEmpty(doneBy) || Long.parseLong(doneBy) <= 0)) {

                                            UUID uuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Immunization, "" + recordIdStr);
                                            if (uuid == null) {
                                                throw new Exception("Failed to find resource UUID for " + ResourceType.Immunization + " " + recordIdStr);
                                            }

                                            tppCsvHelper.getStaffMemberCache().addRequiredStaffId(CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));

                                            Object obj = tppCsvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                                            if (!(obj instanceof String)) {
                                                throw new Exception("Got " + obj.getClass() + " " + obj + " for doneBy " + doneBy + " and doneAt " + doneAt);
                                            }
                                            Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)obj);
                                            reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, fhirResourceFiler);

                                            ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Immunization.toString(), uuid);
                                            Immunization resource = (Immunization)wrapper.getResource();
                                            ImmunizationBuilder builder = new ImmunizationBuilder(resource);

                                            builder.setPerformer(reference);

                                            filer.savePatientResource(null, false, builder);
                                        }

                                        hsImmunisationDone.add(recordId);
                                    }
                                }
                            }


                        }

                    }
*/

                } finally {

                    //close down filer
                    filer.waitToFinish();

                    //set multicast header
                    String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                    newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                    //post to Rabbit protocol queue
                    List<UUID> exchangeIds = new ArrayList<>();
                    exchangeIds.add(newExchange.getId());
                    QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);

                    //set this after posting to rabbit so we can't re-queue it later
                    newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                    newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                    AuditWriter.writeExchange(newExchange);
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished fixing missing TPP practitioner at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static String findFilePath(Exchange exchange, String fileType) {

        String exchangeBody = exchange.getBody();
        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

        for (ExchangePayloadFile file : files) {
            if (file.getType().equals(fileType)) {
                return file.getPath();
            }
        }

        return null;
    }

    private static void doDrugSensitivty(List<Exchange> exchanges, TppCsvHelper csvHelper, FhirResourceFiler filer) throws Exception {

        Set<Long> hsIdsDone = new HashSet<>();

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //the exchanges are ordered most-recent-first, so start at zero and work backwards to do the most recent ones first
        for (int i=0; i<exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);

            String path = findFilePath(exchange, "Immunisation");
            if (path == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String recordIdStr = record.get("RowIdentifier");
                Long recordId = Long.valueOf(recordIdStr);

                //if already done a more recent version of this record
                if (hsIdsDone.contains(recordId)) {
                    continue;
                }
                hsIdsDone.add(recordId);


                String doneBy = record.get("IDDoneBy");
                String doneAt = record.get("IDOrganisationDoneAt");

                //if the done AT is empty, we really don't have any data to use
                if (Strings.isNullOrEmpty(doneAt)) {
                    continue;
                }

                //if the done BY is a valid number, it will have already set the practitioner
                if (!Strings.isNullOrEmpty(doneBy) && Long.parseLong(doneBy) > 0) {
                    continue;
                }

                UUID uuid = IdHelper.getEdsResourceId(filer.getServiceId(), ResourceType.Immunization, "" + recordIdStr);
                if (uuid == null) {
                    throw new Exception("Failed to find resource UUID for " + ResourceType.Immunization + " " + recordIdStr);
                }

                csvHelper.getStaffMemberCache().addRequiredStaffId(CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
/*
                Object obj = tppCsvHelper.getStaffMemberCache().findProfileIdForStaffMemberAndOrg(CsvCell.factoryDummyWrapper(doneBy), CsvCell.factoryDummyWrapper(doneAt));
                if (!(obj instanceof String)) {
                    throw new Exception("Got " + obj.getClass() + " " + obj + " for doneBy " + doneBy + " and doneAt " + doneAt);
                }
                Reference reference = ReferenceHelper.createReference(ResourceType.Practitioner, (String)obj);
                reference = IdHelper.convertLocallyUniqueReferenceToEdsReference(reference, filer);

                ResourceWrapper wrapper = resourceDal.getCurrentVersion(filer.getServiceId(), ResourceType.Immunization.toString(), uuid);
                AllergyIntolerance resource = (AllergyIntolerance)wrapper.getResource();
                AllergyIntoleranceBuilder builder = new AllergyIntoleranceBuilder(resource);

                builder.setPerformer(reference);

                filer.savePatientResource(null, false, builder);*/
            }
        }
    }
}
