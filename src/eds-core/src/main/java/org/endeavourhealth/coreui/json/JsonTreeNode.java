package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTreeNode {
    private String name = null;
    private String id = null;
    private boolean hasChildren = false;
    private Short type = 0;
    private String itemUuid = null;
    private List<JsonTreeNode> children = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public List<JsonTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<JsonTreeNode> children) {
        this.children = children;
    }

    public String getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(String itemUuid) {
        this.itemUuid = itemUuid;
    }
}
