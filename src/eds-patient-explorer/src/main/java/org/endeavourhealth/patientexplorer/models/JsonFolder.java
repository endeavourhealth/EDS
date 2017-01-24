package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.admin.models.Item;

import java.util.UUID;

/**
 * JSON object used to manipulate folders, such as creating, moving and renaming
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonFolder implements Comparable {

    public static final int FOLDER_TYPE_LIBRARY = 1;
    //public static final int FOLDER_TYPE_REPORTS = 2;

    private UUID uuid = null;
    private String folderName = null;
    private Integer folderType = null;
    private UUID parentFolderUuid = null;
    private Boolean hasChildren = null;
    private Integer contentCount = null;

    public JsonFolder() {
    }

    public JsonFolder(Item item, int contentCount, boolean hasChildren) {
        this.uuid = item.getId();
        this.folderName = item.getTitle();

        this.hasChildren = hasChildren;

        if (contentCount > 0) {
            this.contentCount = contentCount;
        }
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getFolderType() {
        return folderType;
    }

    public void setFolderType(Integer folderType) {
        this.folderType = folderType;
    }

    public UUID getParentFolderUuid() {
        return parentFolderUuid;
    }

    public void setParentFolderUuid(UUID parentUuid) {
        this.parentFolderUuid = parentUuid;
    }

    public Integer getContentCount() {
        return contentCount;
    }

    public void setContentCount(Integer contentCount) {
        this.contentCount = contentCount;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @Override
    public int compareTo(Object o) {
        JsonFolder other = (JsonFolder)o;
        return folderName.compareToIgnoreCase(other.folderName);
    }
}
