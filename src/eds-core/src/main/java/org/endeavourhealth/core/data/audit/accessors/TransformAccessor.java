package org.endeavourhealth.core.data.audit.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformErrorToReProcess;

import java.util.UUID;

@Accessor
public interface TransformAccessor {

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


    @Query("SELECT * FROM audit.exchange_transform_errors_to_re_process WHERE service_id = :service_id AND system_id = :system_id")
    Result<ExchangeTransformErrorToReProcess> getErrorsToReProcess(@Param("service_id") UUID serviceId,
                                                                   @Param("system_id") UUID systemId);
}
