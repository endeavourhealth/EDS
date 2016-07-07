package org.endeavourhealth.ui.json;

import java.util.List;
import java.util.UUID;

public final class JsonMoveItems {
    private UUID destinationFolder = null;
    private List<JsonMoveItem> items = null;

    /**
     * gets/sets
     */
    public UUID getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(UUID destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public List<JsonMoveItem> getItems() {
        return items;
    }

    public void setItems(List<JsonMoveItem> items) {
        this.items = items;
    }
}
