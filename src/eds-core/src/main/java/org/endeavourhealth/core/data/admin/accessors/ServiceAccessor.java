package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.OrganisationServiceLink;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.UUID;

@Accessor
public interface ServiceAccessor {
	@Query("SELECT * FROM admin.service")
	Result<Service> getAll();

	@Query("SELECT * FROM admin.organisation_service_link_by_service_id WHERE service_id = :service_id")
	Result<OrganisationServiceLink> getOrganisationServiceLinkByServiceId(@Param("service_id") UUID serviceId);
}
