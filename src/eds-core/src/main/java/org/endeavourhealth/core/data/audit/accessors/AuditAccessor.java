package org.endeavourhealth.core.data.audit.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.audit.models.*;

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

@Accessor
public interface AuditAccessor {

    @Query("SELECT * FROM audit.exchange_transform_audit WHERE service_id = :service_id AND system_id = :system_id AND exchange_id = :exchange_id LIMIT 1")
    Result<ExchangeTransformAudit> getMostRecentExchangeTransform(@Param("service_id") UUID serviceId,
                                                                  @Param("system_id") UUID systemId,
                                                                  @Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM audit.exchange_transform_audit WHERE service_id = :service_id AND system_id = :system_id AND exchange_id = :exchange_id")
    Result<ExchangeTransformAudit> getAllExchangeTransform(@Param("service_id") UUID serviceId,
                                                           @Param("system_id") UUID systemId,
                                                           @Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM audit.exchange_transform_error_state WHERE service_id = :service_id AND system_id = :system_id LIMIT 1")
    Result<ExchangeTransformErrorState> getErrorState(@Param("service_id") UUID serviceId,
                                                     @Param("system_id") UUID systemId);

    @Query("SELECT * FROM audit.exchange_transform_error_state")
    Result<ExchangeTransformErrorState> getAllErrorStates();

    @Query("SELECT * FROM audit.exchange_transform_audit WHERE service_id = :service_id AND system_id = :system_id LIMIT 1")
    Result<ExchangeTransformAudit> getFirstExchangeTransformAudit(@Param("service_id") UUID serviceId,
                                                                  @Param("system_id") UUID systemId);

    @Query("SELECT * FROM audit.exchange_transform_audit WHERE service_id = :service_id AND system_id = :system_id")
    Result<ExchangeTransformAudit> getAllExchangeTransformAudits(@Param("service_id") UUID serviceId,
                                                                @Param("system_id") UUID systemId);

    @Query("SELECT * FROM audit.exchange_transform_audit WHERE service_id = :service_id AND system_id = :system_id AND exchange_id = :exchange_id")
    Result<ExchangeTransformAudit> getAllExchangeTransformAudits(@Param("service_id") UUID serviceId,
                                                                 @Param("system_id") UUID systemId,
                                                                 @Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM audit.exchange ALLOW FILTERING;")
    Result<Exchange> getAllExchanges();

    @Query("SELECT * FROM audit.exchange_by_service WHERE service_id = :service_id LIMIT :num_rows")
    Result<ExchangeByService> getExchangesByService(@Param("service_id") UUID serviceId,
                                                    @Param("num_rows") int numRows);

    @Query("SELECT * FROM audit.exchange_by_service WHERE service_id = :service_id AND timestamp >= :date_from AND timestamp <= :date_to LIMIT :num_rows")
    Result<ExchangeByService> getExchangesByService(@Param("service_id") UUID serviceId,
                                                    @Param("num_rows") int numRows,
                                                    @Param("date_from") Date dateFrom,
                                                    @Param("date_to") Date dateTo);

/*    @Query("SELECT * FROM audit.exchange_by_service WHERE service_id = :service_id AND timestamp >= :date_from AND timestamp <= :date_to LIMIT :num_rows")
    Result<ExchangeByService> getExchangesByService(@Param("service_id") UUID serviceId,
                                                    @Param("num_rows") int numRows,
                                                    @Param("date_from") String dateFrom,
                                                    @Param("date_to") String dateTo);*/

    @Query("SELECT * FROM audit.exchange_event WHERE exchange_id = :exchange_id")
    Result<ExchangeEvent> getExchangeEvents(@Param("exchange_id") UUID exchangeId);

}
