package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;


import java.util.UUID;

@Table(keyspace = "admin", name = "active_item")
public class ActiveItem {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "audit_id")
    private UUID auditId;
    @Column(name = "organisation_id")
    private UUID organisationId;
    @Column(name = "item_id")
    private UUID itemId;
    @Column(name = "item_type_id")
    private Integer itemTypeId;
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public static ActiveItem factoryNew(Item item, UUID organisationUuid, DefinitionItemType itemType) {
        UUID itemUuid = item.getId();
        UUID auditUuid = item.getAuditId();

        if (itemUuid == null) {
            throw new RuntimeException("Cannot create ActiveItem without first saving Item to DB");
        }

        ActiveItem ret = new ActiveItem();
        ret.setOrganisationId(organisationUuid);
        ret.setItemId(itemUuid);
        ret.setAuditId(auditUuid);
        ret.setItemTypeId(itemType.getValue());
        ret.setId(UUID.randomUUID());
        ret.setIsDeleted(false);

        return ret;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}