package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.admin.accessors.LibraryAccessor;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.ItemDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class LibraryRepository extends Repository {
		private static final Logger LOG = LoggerFactory.getLogger(LibraryRepository.class);

    public void save(List<Object> entities){

        Mapper<Item> mapperLibraryItem = getMappingManager().mapper(Item.class);
        Mapper<Audit> mapperAudit = getMappingManager().mapper(Audit.class);
        Mapper<ActiveItem> mapperActiveItem = getMappingManager().mapper(ActiveItem.class);
        Mapper<ItemDependency> mapperItemDependency = getMappingManager().mapper(ItemDependency.class);

        BatchStatement batch = new BatchStatement();

        for (Object entity: entities) {

            if (entity.getClass() == Audit.class) {
                batch.add(mapperAudit.saveQuery((Audit) entity));
            }
            else if (entity.getClass() == Item.class) {
                batch.add(mapperLibraryItem.saveQuery((Item) entity));
            }
            else if (entity.getClass() == ActiveItem.class) {
                batch.add(mapperActiveItem.saveQuery((ActiveItem) entity));
            }
            else if (entity.getClass() == ItemDependency.class) {
                batch.add(mapperItemDependency.saveQuery((ItemDependency) entity));
            }

        }

        LOG.trace("Saving batch of " + batch.size() + " items.");

        getSession().execute(batch);
    }

    public Item getItemByKey(UUID id, UUID auditId) {
        Mapper<Item> mapperLibraryItem = getMappingManager().mapper(Item.class);
        return mapperLibraryItem.get(id, auditId);
    }

    public Audit getAuditByKey(UUID id) {
        Mapper<Audit> mapperLibraryItem = getMappingManager().mapper(Audit.class);
        return mapperLibraryItem.get(id);
    }

    public ActiveItem getActiveItemByItemId(UUID itemId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getActiveItemByItemId(itemId);
    }

    public Iterable<ActiveItem> getActiveItemByOrgAndTypeId(UUID organisationId, Integer itemTypeId, Boolean isDeleted) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getActiveItemByOrgAndTypeId(organisationId, itemTypeId, isDeleted);
    }

    public Iterable<ActiveItem> getActiveItemByTypeId(Integer itemTypeId, Boolean isDeleted) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getActiveItemByTypeId(itemTypeId, isDeleted);
    }

    public Iterable<ActiveItem> getActiveItemByOrg(UUID organisationId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getActiveItemByOrg(organisationId);
    }

    public Iterable<ActiveItem> getActiveItemByAuditId(UUID auditId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getActiveItemByAuditId(auditId);
    }

    public Iterable<ItemDependency> getItemDependencyByItemId(UUID itemId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getItemDependencyByItemId(itemId);
    }

    public Iterable<ItemDependency> getItemDependencyByTypeId(UUID itemId, UUID auditId, Integer dependencyTypeId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getItemDependencyByTypeId(itemId, auditId, dependencyTypeId);
    }

    public Iterable<ItemDependency> getItemDependencyByDependentItemId(UUID dependentItemId, Integer dependencyTypeId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getItemDependencyByDependentItemId(dependentItemId, dependencyTypeId);
    }

    public Iterable<Audit> getAuditByOrgAndDateDesc(UUID organisationId) {
        LibraryAccessor accessor = getMappingManager().createAccessor(LibraryAccessor.class);
        return accessor.getAuditByOrgAndDateDesc(organisationId);
    }



}

