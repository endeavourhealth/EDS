package org.endeavourhealth.core.data.audit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.audit.accessors.AuditAccessor;
import org.endeavourhealth.core.data.audit.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AuditRepository extends Repository{

    private static final Logger LOG = LoggerFactory.getLogger(AuditRepository.class);

    public void save(Exchange exchange) {
        Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
        mapperExchange.save(exchange);
    }

    public void save(ExchangeEvent event) {
        Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
        mapperEvent.save(event);
    }

    /*public void save(Exchange exchange, ExchangeEvent event) {

        if (exchange != null) {
            //NOTE: the exchanges can be large, so don't use a batch to save it, as it will fail
            Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
            mapperExchange.save(exchange);
        }

        if (event != null) {
            Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
            mapperEvent.save(event);
        }
    }*/

    public Exchange getExchange(UUID exchangeId) {
        Mapper<Exchange> mapper = getMappingManager().mapper(Exchange.class);
        return mapper.get(exchangeId);
    }

    public void save(ExchangeTransformAudit exchangeTransformAudit) {

        Mapper<ExchangeTransformAudit> mapper = getMappingManager().mapper(ExchangeTransformAudit.class);
        mapper.save(exchangeTransformAudit);
    }

    public void save(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState.class);
        mapper.save(errorState);
    }

    public void save(ExchangeByService exchangeByService) {

        Mapper<ExchangeByService> mapper = getMappingManager().mapper(ExchangeByService.class);
        mapper.save(exchangeByService);
    }

    public void delete(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState .class);
        mapper.delete(errorState);
    }

    public ExchangeTransformAudit getMostRecentExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformAudit> iterator = accessor.getMostRecentExchangeTransform(serviceId, systemId, exchangeId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransformAudit> getAllExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransform(serviceId, systemId, exchangeId));
    }

    public ExchangeTransformErrorState getErrorState(UUID serviceId, UUID systemId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformErrorState> iterator = accessor.getErrorState(serviceId, systemId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransformErrorState> getAllErrorStates() {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllErrorStates());
    }

    public boolean isServiceStarted(UUID serviceId, UUID systemId) {

        //find the FIRST exchange we received for the parameters
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformAudit> iterator = accessor.getFirstExchangeTransformAudit(serviceId, systemId).iterator();

        //if we've never transformed an exchange for this service/system before, then it's definitely not started
        if (!iterator.hasNext()) {
            return false;
        }

        ExchangeTransformAudit firstExchangeAudit = iterator.next();

        //if the first exchange transform has been deleted (i.e. the data was deleted from the EHR DB), we need
        //to find the first non-deleted one to see if the data has been re-played
        if (firstExchangeAudit.getDeleted() != null) {
            for (ExchangeTransformAudit audit: getAllExchangeTransformAudits(serviceId, systemId)) {
                if (audit.getDeleted() == null) {
                    firstExchangeAudit = audit;
                    break;
                }
            }

            //if we've not got a non-deleted one, then we've not restarted the service
            if (firstExchangeAudit.getDeleted() != null) {
                return false;
            }
        }

        //if we have processed an exchange for the service/system, then make sure it was processed ok
        if (firstExchangeAudit.getErrorXml() == null) {
            return true;
        }

        //if it wasn't processed ok, then make sure there was a subsequent audit of that same exchange being processed ok
        iterator = accessor.getMostRecentExchangeTransform(serviceId, systemId, firstExchangeAudit.getExchangeId()).iterator();
        ExchangeTransformAudit subsequentExchangeAudit = iterator.next();
        if (subsequentExchangeAudit.getErrorXml() == null) {
            return true;
        }

        //if the first exchange for our service/system was never processed OK, we've not properly started receiving for this service
        return false;
    }

    public List<ExchangeTransformAudit> getAllExchangeTransformAudits(UUID serviceId, UUID systemId) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransformAudits(serviceId, systemId));
    }

    public List<ExchangeTransformAudit> getAllExchangeTransformAudits(UUID serviceId, UUID systemId, UUID exchangeId) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransformAudits(serviceId, systemId, exchangeId));
    }

    /*public List<Exchange> getAllExchanges() {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllExchanges());
    }*/

    public List<ExchangeByService> getExchangesByService(UUID serviceId, int maxRows, Date dateFrom, Date dateTo) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getExchangesByService(serviceId, maxRows, dateFrom, dateTo));
    }

    public List<ExchangeByService> getExchangesByService(UUID serviceId, int maxRows) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getExchangesByService(serviceId, maxRows));
    }

    public List<ExchangeEvent> getExchangeEvents(UUID exchangeId) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getExchangeEvents(exchangeId));
    }

    public ExchangeTransformAudit getExchangeTransformAudit(UUID serviceId, UUID systemId, UUID exchangeId, UUID versionUuid) {
        Mapper<ExchangeTransformAudit> mapper = getMappingManager().mapper(ExchangeTransformAudit.class);
        return mapper.get(serviceId, systemId, exchangeId, versionUuid);
    }

    public List<UUID> getExchangeIdsForService(UUID serviceId) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        ResultSet resultSet = accessor.getExchangeIdsForService(serviceId);

        List<UUID> ret = new ArrayList<>();
        while (!resultSet.isExhausted()) {
            Row row = resultSet.one();
            UUID uuid = row.getUUID(0);
            ret.add(uuid);
        }

        //the accessor returns them most recent first, so reverse it so they're most recent last
        return Lists.reverse(ret);
    }
}
