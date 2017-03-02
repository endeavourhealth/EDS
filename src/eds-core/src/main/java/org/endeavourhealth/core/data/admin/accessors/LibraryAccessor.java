package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.ItemDependency;

import java.util.UUID;

@Accessor
public interface LibraryAccessor {
    @Query("SELECT * FROM admin.active_item_by_item_id WHERE item_id = :item_id")
    ActiveItem getActiveItemByItemId(@Param("item_id") UUID itemId);

    @Query("SELECT * FROM admin.active_item_by_audit_id WHERE audit_id = :audit_id")
    Result<ActiveItem> getActiveItemByAuditId(@Param("audit_id") UUID audit_id);

    @Query("SELECT * FROM admin.active_item_by_org_and_type WHERE organisation_id = :organisation_id and item_type_id = :item_type_id and is_deleted = :is_deleted")
    Result<ActiveItem> getActiveItemByOrgAndTypeId(@Param("organisation_id") UUID organisationId, @Param("item_type_id") Integer itemTypeId, @Param("is_deleted") Boolean isDeleted);

    @Query("SELECT * FROM admin.active_item_by_type WHERE item_type_id = :item_type_id and is_deleted = :is_deleted")
    Result<ActiveItem> getActiveItemByTypeId(@Param("item_type_id") Integer itemTypeId, @Param("is_deleted") Boolean isDeleted);

    @Query("SELECT * FROM admin.active_item_by_org WHERE organisation_id = :organisation_id and is_deleted = false limit 5")
    Result<ActiveItem> getActiveItemByOrg(@Param("organisation_id") UUID organisationId);

    @Query("SELECT * FROM admin.item_dependency WHERE item_id = :item_id")
    Result<ItemDependency> getItemDependencyByItemId(@Param("item_id") UUID itemId);

    @Query("SELECT * FROM admin.item_dependency_by_type WHERE item_id = :item_id and audit_id = :audit_id and dependency_type_id = :dependency_type_id")
    Result<ItemDependency> getItemDependencyByTypeId(@Param("item_id") UUID itemId, @Param("audit_id") UUID auditId, @Param("dependency_type_id") Integer dependencyTypeId);

    @Query("SELECT * FROM admin.item_dependency_by_dependent_item_id WHERE dependent_item_id = :dependent_item_id and dependency_type_id = :dependency_type_id")
    Result<ItemDependency> getItemDependencyByDependentItemId(@Param("dependent_item_id") UUID dependentItemId, @Param("dependency_type_id") Integer dependencyTypeId);

    @Query("SELECT * FROM admin.audit_by_org_and_date_desc WHERE organisation_id = :organisation_id limit 5")
    Result<Audit> getAuditByOrgAndDateDesc(@Param("organisation_id") UUID organisationId);

}
