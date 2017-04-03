package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.Service;

@Accessor
public interface ServiceAccessor {
	@Query("SELECT * FROM admin.service")
	Result<Service> getAll();

	@Query("SELECT * FROM admin.service WHERE name >= :searchData AND name < :rangeEnd allow filtering")
	Result<Service> search(@Param("searchData") String searchData, @Param("rangeEnd") String rangeEnd);

	@Query("SELECT * FROM admin.service_by_local_identifier WHERE local_identifier = :localIdentifier")
	Result<Service> getByLocalIdentifier(@Param("localIdentifier") String localIdentifier);
}
