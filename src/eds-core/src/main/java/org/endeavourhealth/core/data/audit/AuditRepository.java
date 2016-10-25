package org.endeavourhealth.core.data.audit;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.audit.accessors.AuditAccessor;
import org.endeavourhealth.core.data.audit.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AuditRepository extends Repository{

    private static final Logger LOG = LoggerFactory.getLogger(AuditRepository.class);


    public void save(Exchange exchange, ExchangeEvent event) {

        //exchange will only be non-null when writing the first event for an exchange
        if (exchange != null) {
            //NOTE: the exchanges can be large, so don't use a batch to save it, as it will fail
            Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
            mapperExchange.save(exchange);
        }


        Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
        mapperEvent.save(event);
    }

    public void save(ExchangeTransformAudit exchangeTransformAudit) {

        Mapper<ExchangeTransformAudit> mapper = getMappingManager().mapper(ExchangeTransformAudit.class);
        mapper.save(exchangeTransformAudit);
    }

    public void save(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState .class);
        mapper.save(errorState);
    }

    public void delete(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState .class);
        mapper.delete(errorState);
    }

    public void save(ExchangeTransformErrorToReProcess toReProcess) {

        Mapper<ExchangeTransformErrorToReProcess> mapper = getMappingManager().mapper(ExchangeTransformErrorToReProcess.class);
        mapper.save(toReProcess);
    }

    public void delete(ExchangeTransformErrorToReProcess toReProcess) {

        Mapper<ExchangeTransformErrorToReProcess> mapper = getMappingManager().mapper(ExchangeTransformErrorToReProcess.class);
        mapper.delete(toReProcess);
    }

    public List<UUID> getExchangeUuidsToReProcess(ExchangeTransformErrorState errorState) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformErrorToReProcess> iterator = accessor.getErrorsToReProcess(errorState.getServiceId(), errorState.getSystemId()).iterator();

        List<UUID> ret = new ArrayList<>();
        while (iterator.hasNext()) {
            ExchangeTransformErrorToReProcess o = iterator.next();
            ret.add(o.getExchangeId());
        }
        return ret;
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

    public void deleteExchangeIdToReProcess(ExchangeTransformErrorState errorState, UUID exchangeId) {
        ExchangeTransformErrorToReProcess o = new ExchangeTransformErrorToReProcess();
        o.setServiceId(errorState.getServiceId());
        o.setSystemId(errorState.getSystemId());
        o.setExchangeId(exchangeId);
        delete(o);
    }

    public void startServiceIfRequired(UUID serviceId, UUID systemId) {

        if (!isServiceStarted(serviceId, systemId)) {
            ServiceStart serviceStart = new ServiceStart();
            serviceStart.setServiceId(serviceId);
            serviceStart.setSystemId(systemId);
            serviceStart.setStarted(new Date());
            save(serviceStart);
        }
    }

    public void save(ServiceStart serviceStart) {

        Mapper<ServiceStart> mapperEvent = getMappingManager().mapper(ServiceStart.class);
        mapperEvent.save(serviceStart);
    }

    public boolean isServiceStarted(UUID serviceId, UUID systemId) {
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ServiceStart> iterator = accessor.getServiceStart(serviceId, systemId).iterator();
        if (iterator.hasNext()) {
            return true;
        } else {
            return false;
        }
    }
}
