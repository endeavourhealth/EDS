package org.endeavourhealth.core.data.audit.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.audit.models.ExchangeTransform;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformByServiceAndSystem;

import java.util.UUID;

@Accessor
public interface ExchangeAccessor {

    @Query("SELECT * FROM audit.exchange_transform WHERE service_id = :service_id AND system_id = :system_id AND exchange_id = :exchange_id LIMIT 1")
    Result<ExchangeTransform> getMostRecentExchangeTransform(@Param("service_id") UUID serviceId,
                                                             @Param("system_id") UUID systemId,
                                                             @Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM audit.exchange_transform WHERE service_id = :service_id AND system_id = :system_id AND exchange_id = :exchange_id")
    Result<ExchangeTransform> getAllExchangeTransform(@Param("service_id") UUID serviceId,
                                                             @Param("system_id") UUID systemId,
                                                             @Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM audit.exchange_transform_by_service_and_system WHERE service_id = :service_id AND system_id = :system_id LIMIT 1")
    Result<ExchangeTransformByServiceAndSystem> getMostRecentExchangeTransformByServiceAndSystem(@Param("service_id") UUID serviceId,
                                                                                                 @Param("system_id") UUID systemId);

    @Query("SELECT * FROM audit.exchange_transform_by_service_and_system WHERE service_id = :service_id AND system_id = :system_id")
    Result<ExchangeTransformByServiceAndSystem> getAllExchangeTransformByServiceAndSystem(@Param("service_id") UUID serviceId,
                                                                                                 @Param("system_id") UUID systemId);

}
