package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.PeriodHelper;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.EncounterBuilder;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SD511 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD511.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP Encounter-Episode links (SD-511)";

    /**
     * fixes data SD-511
     *
     * problem is that the TPP inbound transform linked FHIR Encounters with the wrong Episode ID
     *
     */
    public static void fixTppEncounterEpisodeLinks(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing TPP Encounter to Episode links at " + odsCodeRegex + " test mode = " + testMode);

        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode) {
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixTppEncounterEpisodeLinksAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

            }

            LOG.debug("Finished Fixing TPP Local Codes at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppEncounterEpisodeLinksAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        UUID exchangeId = UUID.randomUUID();

        //only affects patients at multiple TPP orgs, where the orgs went through the same inbound queue
        //need to factor in the TPP community sites that changed ODS code!!!
        Set<UUID> patientIds = findAffectedPatientIds(serviceId);
        LOG.debug("Found " + patientIds.size() + " potentially affected patients");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing encounter links");
            fixEncounterLinks(serviceId, patientIds, filer);

        } catch (Throwable ex) {
            LOG.error("Error doing service " + service, ex);
            throw ex;

        } finally {

            //close down filer
            if (filer != null) {
                LOG.debug("Waiting to finish");
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

            //we'll have a load of stuff cached in here, so clear it down as it won't be applicable to the next service
            IdHelper.clearCache();
        }


    }

    private static void fixEncounterLinks(UUID serviceId, Set<UUID> patientIds, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (UUID patientId: patientIds) {

            //find what the episode IDs should be
            Map<UUID, EpisodeOfCare> hmEpisodes = new HashMap<>();

            List<ResourceWrapper> episodeWrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.EpisodeOfCare.toString());
            for (ResourceWrapper episodeWrapper : episodeWrappers) {
                UUID episodeId = episodeWrapper.getResourceId();
                EpisodeOfCare episodeOfCare = (EpisodeOfCare)episodeWrapper.getResource();
                hmEpisodes.put(episodeId, episodeOfCare);
            }

            //retrieve encounters
            List<ResourceWrapper> encounterWrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Encounter.toString());

            //fix all encounters pointing to non-existent episodes
            for (ResourceWrapper encounterWrapper : encounterWrappers) {
                Encounter encounter = (Encounter) encounterWrapper.getResource();

                boolean needsFixing = false;
                if (!encounter.hasEpisodeOfCare()) {
                    //part of the bug meant this wasn't set at all
                    needsFixing = true;

                } else {
                    //other part of the bug is them pointing to a non-existant episode
                    Reference episodeReference = encounter.getEpisodeOfCare().get(0); //only ever add one
                    String episodeIdStr = ReferenceHelper.getReferenceId(episodeReference);
                    if (!hmEpisodes.containsKey(UUID.fromString(episodeIdStr))) {
                        needsFixing = true;
                    }
                }

                if (!needsFixing) {
                    continue;
                }

                //need to find the episode ID that it should be
                UUID episodeId = findBestEpisodeId(encounter, hmEpisodes);
                if (episodeId == null) {
                    throw new Exception("Failed to find episode ID for patient " + patientId);
                }

                Reference ref = ReferenceHelper.createReference(ResourceType.EpisodeOfCare, episodeId.toString());
                EncounterBuilder builder = new EncounterBuilder(encounter);
                builder.setEpisodeOfCare(ref);

                if (filer == null) {
                    LOG.debug("Fixed encounter " + encounter.getId() + " with new episode " + episodeId);

                } else {
                    filer.savePatientResource(null, false, builder);
                }

                changed ++;
            }

            done++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed + " resources");
            }

        }
        LOG.debug("Finished " + done + " patients, fixed " + changed + " resources");
    }

    private static UUID findBestEpisodeId(Encounter encounter, Map<UUID, EpisodeOfCare> hmEpisodes) {

        //find the one that would have been active on the encounter date
        Date encounterDate = null;
        if (encounter.hasPeriod()) {
            Period period = encounter.getPeriod();
            DateTimeType dt = period.getStartElement();
            encounterDate = dt.getValue();
        }

        if (encounterDate != null) {
            for (UUID episodeId: hmEpisodes.keySet()) {
                EpisodeOfCare episode = hmEpisodes.get(episodeId);

                if (PeriodHelper.isWithin(episode.getPeriod(), encounterDate)) {
                    return episodeId;
                }
            }
        }

        //if we didn't find a match above, fall back and use the most recently active one
        Date mostRecentEndDate = null;
        UUID mostRecentEndedId = null;

        for (UUID episodeId: hmEpisodes.keySet()) {
            EpisodeOfCare episode = hmEpisodes.get(episodeId);

            Date endDate = null;
            if (episode.hasPeriod()) {
                endDate = episode.getPeriod().getEnd();
            }

            //if no end date, then this episode is active and should be the one used
            if (endDate == null) {
                return episodeId;
            }

            if (mostRecentEndedId == null
                    || endDate.after(mostRecentEndDate)) {
                mostRecentEndedId = episodeId;
                mostRecentEndDate = endDate;
            }
        }

        return mostRecentEndedId;
    }

    /**
     * a table of affected patients was created (see SD-511 Jira ticket for the SQL) to give us the patients potentially affected
     */
    private static Set<UUID> findAffectedPatientIds(UUID serviceId) throws Exception {

        Connection connection = ConnectionManager.getAdminConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT patient_id FROM tmp.SD511_affected_patients WHERE service_id = ?";
            ps = connection.prepareStatement(sql);

            ps.setString(1, serviceId.toString());

            Set<UUID> ret = new HashSet<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String patientIdStr = rs.getString(1);
                ret.add(UUID.fromString(patientIdStr));
            }

            return ret;

        } finally {
            ps.close();
            connection.close();
        }

    }




}