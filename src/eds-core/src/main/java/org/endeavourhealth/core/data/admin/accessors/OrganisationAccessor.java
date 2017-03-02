package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.OrganisationEndUserLink;

import java.util.UUID;

@Accessor
public interface OrganisationAccessor {
	@Query("SELECT * FROM admin.organisation")
	Result<Organisation> getAll();

	@Query("SELECT * FROM admin.organisation_end_user_link_by_user_id WHERE end_user_id = :end_user_id")
	Result<OrganisationEndUserLink> getOrganisationEndUserLinkByEndUserId(@Param("end_user_id") UUID endUserId);

	@Query("SELECT * FROM admin.organisation WHERE name >= :searchData AND name < :rangeEnd allow filtering")
	Result<Organisation> search(@Param("searchData") String searchData, @Param("rangeEnd") String rangeEnd);

	@Query("SELECT * FROM admin.organisation_by_national_id WHERE national_id = :nationalId")
	Result<Organisation> getByNationalId(@Param("nationalId") String nationalId);

}
