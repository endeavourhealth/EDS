package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.core.data.admin.models.Item;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonFolderContent implements Comparable {
    private UUID uuid = null;
    private Integer type = null;
    private String typeDesc = null;
    private String name = null;
    private String description = null;
    private Date lastModified = null;
    private Date lastRun = null; //only applicable when showing reports
    private Boolean isScheduled = null; //only applicable when showing reports

    public JsonFolderContent() {

    }

    public JsonFolderContent(ActiveItem activeItem, Item item, Audit audit) {
        this(item, audit);
        setTypeEnum(DefinitionItemType.get(activeItem.getItemTypeId()));
    }

    public JsonFolderContent(Item item, Audit audit) {
        this.uuid = item.getId();
        this.name = item.getTitle();
        this.description = item.getDescription();

        if (audit != null) {
            this.lastModified = audit.getTimeStamp();
        }
    }

    public void setTypeEnum(DefinitionItemType t) {
        setType(t.getValue());
        setTypeDesc(t.toString());
    }


    /**
     * gets/sets
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModified() {
        return this.lastModified == null ? null : new Date(lastModified.getTime());
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
    }

    public Date getLastRun() {
        return this.lastRun == null ? null : new Date(lastRun.getTime());
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun == null ? null : new Date(lastRun.getTime());
    }

    public Boolean getIsScheduled() {
        return isScheduled;
    }

    public void setIsScheduled(Boolean scheduled) {
        isScheduled = scheduled;
    }

    @Override
    public int compareTo(Object o) {
        JsonFolderContent other = (JsonFolderContent)o;
        return name.compareToIgnoreCase(other.name);
    }
}
