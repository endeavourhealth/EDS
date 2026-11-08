package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.MedicationOrderBuilder;
import org.endeavourhealth.transform.tpp.csv.helpers.TppCsvHelper;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SD529 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD511.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP MedicationOrder-Statement links (SD-529)";

    /**
     * fixes data SD-529
     *
     * problem is that the TPP inbound transform didn't link MedicationOrders to MedicationStatements correctly for acute medication
     */
    public static void SD529fixTppMedicationOrderToStatementLinks(boolean includeStartedButNotFinishedServices, boolean testMode, int minsDelay, String odsCodeRegex, UUID patientUuidFilter) {
        LOG.debug("Fixing TPP MedicationOrder to Statement links at " + odsCodeRegex + " test mode = " + testMode);

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
                fixTppMedicationOrderToStatementLinksAtService(testMode, service, patientUuidFilter);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);

                    LOG.debug("Waiting " + minsDelay + " mins");
                    Thread.sleep(minsDelay * 60 * 1000);

                }

            }

            LOG.debug("Finished Fixing TPP Local Codes at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppMedicationOrderToStatementLinksAtService(boolean testMode, Service service, UUID patientUuidFilter) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        UUID exchangeId = UUID.randomUUID();

        List<UUID> patientIds = findPatientIds(serviceId);
        if (patientUuidFilter != null) {
            patientIds.clear();
            patientIds.add(patientUuidFilter);
        }
        LOG.debug("Found " + patientIds.size() + " patient IDs");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created: " + BULK_OPERATION_NAME, exchangeId);
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing links");
            fixMedicationOrderLinks(serviceId, patientIds, filer);

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

    private static void fixMedicationOrderLinks(UUID serviceId, List<UUID> patientIds, FhirResourceFiler filer) throws Exception {

        int done = 0;
        int changed = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        TppCsvHelper helper = new TppCsvHelper(serviceId, null, null);

        for (UUID patientId: patientIds) {
            List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.MedicationOrder.toString());
            if (filer == null) {
                LOG.debug("Doing patient " + patientId + " with " + wrappers.size() + " orders >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }

            for (ResourceWrapper wrapper: wrappers) {

                MedicationOrder order = (MedicationOrder)wrapper.getResource();
                MedicationOrderBuilder builder = new MedicationOrderBuilder(order);

                //get the medication statement reference and convert back to the raw ID
                Reference ref = builder.getMedicationStatementReference();
                //LOG.debug("Statement ref = " + ref.getReference());
                ref = IdHelper.convertEdsReferenceToLocallyUniqueReference(helper, ref);
                //LOG.debug("Converted to raw ID ref = " + ref.getReference());
                String repeatId = ReferenceHelper.getReferenceId(ref);
                //LOG.debug("Order " + order.getId() + " has Statement ref " + repeatId);

                //if the raw is bad, then the raw ID originally used should have been the same as the SRPrimaryCareMedication record ID
                if (!Strings.isNullOrEmpty(repeatId)
                    && !repeatId.equalsIgnoreCase("RepeatTemplate:-1")) { //the specific raw ID
                    //LOG.debug("Does not need fixing");
                    continue;
                }

                //find the original record ID for the SRPrimaryCareMedication
                Reference rowIdRef = ReferenceHelper.createReference(ResourceType.MedicationOrder, order.getId());
                rowIdRef = IdHelper.convertEdsReferenceToLocallyUniqueReference(helper, rowIdRef);
                String rowId = ReferenceHelper.getReferenceId(rowIdRef);

                //and forwards map that to a DDS UUID of a MedicationStatement
                Reference mappedIdRef = ReferenceHelper.createReference(ResourceType.MedicationStatement, rowId);
                mappedIdRef = IdHelper.convertLocallyUniqueReferenceToEdsReference(mappedIdRef, helper);

                builder.setMedicationStatementReference(mappedIdRef);
                changed ++;

                if (filer != null) {
                    filer.savePatientResource(null, false, builder);

                } else {
                    LOG.debug("Order " + order.getId() + " (row ID " + rowId + ") had Statement ref " + repeatId + " so has been changed to " + mappedIdRef.getReference());
                }
            }

            done++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed + " resources");
            }

        }
        LOG.debug("Finished " + done + " patients, fixed " + changed + " resources");
    }


    private static List<UUID> findPatientIds(UUID serviceId) throws Exception {

        PatientSearchDalI dal = DalProvider.factoryPatientSearchDal();
        return dal.getPatientIds(serviceId, false);
    }



}