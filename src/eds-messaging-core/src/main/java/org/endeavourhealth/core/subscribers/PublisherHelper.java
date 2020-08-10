package org.endeavourhealth.core.subscribers;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ServicePublisherAuditDalI;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublisherHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherHelper.class);

    private static Map<UUID, Boolean> cachedLatestDpaState = new HashMap<>();
    private static ExpiringObject<Boolean> cachedUseDsm = new ExpiringObject<>(60 * 1000);

    public static boolean hasDpa(UUID exchangeId, UUID serviceId, String odsCode) throws Exception {

        boolean hasDpa = hasDpaImpl(serviceId, odsCode);

        //audit if this state has changed
        auditDpaStateChange(exchangeId, serviceId, hasDpa);

        return hasDpa;
    }

    /**
     * if the DPA state has changed from what we last audited, then update the audit
     */
    private static void auditDpaStateChange(UUID exchangeId, UUID serviceId, boolean hasDpa) throws Exception {
        ServicePublisherAuditDalI dal = DalProvider.factoryServicePublisherAuditDal();

        Boolean latest = cachedLatestDpaState.get(serviceId);
        if (latest == null) {
            latest = dal.getLatestDpaState(serviceId);
        }

        if (latest == null
                || (latest.booleanValue() != hasDpa)) {
            dal.saveDpaState(UUID.randomUUID(), hasDpa);

            //send Slack message so we know something has changed
            ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
            Service service = serviceDalI.getById(serviceId);
            String msg = "DPA state for " + service.getName() + " " + service.getLocalId() + " has changed on exchange " + exchangeId + ":\r\n"
                    + "" + latest + " -> " + hasDpa;
            SlackHelper.sendSlackMessage(SlackHelper.Channel.MessagingApi, msg);
        }

        cachedLatestDpaState.put(serviceId, Boolean.valueOf(hasDpa));
    }

    private static boolean hasDpaImpl(UUID serviceId, String odsCode) throws Exception {

        if (useDsmForDPAs()) {
            return hasDpaUsingDsm(serviceId, odsCode);

        } else {
            return hasDpaUsingOldProtocols(serviceId, odsCode);
        }
    }

    public static boolean hasDpaUsingDsm(UUID serviceId, String odsCode) throws Exception {
        return OrganisationCache.doesOrganisationHaveDPA(odsCode);
    }

    public static boolean hasDpaUsingOldProtocols(UUID serviceId, String odsCode) throws Exception {

        List<LibraryItem> protocolsOldWay = SubscriberHelper.getProtocolsForPublisherServiceOldWay(serviceId);
        return !protocolsOldWay.isEmpty(); //in the old way, we count as having a DPA if they're in any protocol
    }

    private static boolean useDsmForDPAs() throws Exception {
        Boolean b = cachedUseDsm.get();
        if (b == null) {

            JsonNode json = ConfigManager.getConfigurationAsJson("dsm");
            if (json == null
                    || !json.has("useDsmForDPAs")) {
                b = Boolean.FALSE;

            } else {
                boolean useDsm = json.get("useDsmForDPAs").asBoolean();
                b = Boolean.valueOf(useDsm);
            }
            LOG.debug("Refreshed cache to use DSM for DPAs = " + b);
            cachedUseDsm.set(b);
        }

        return b.booleanValue();
    }
}
