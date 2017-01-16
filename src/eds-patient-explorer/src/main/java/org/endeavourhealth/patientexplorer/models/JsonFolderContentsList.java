package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonFolderContentsList {
    private List<JsonFolderContent> contents = null;

    public JsonFolderContentsList() {
    }



    public void addContent(JsonFolderContent content) {
        if (contents == null) {
            contents = new ArrayList<JsonFolderContent>();
        }
        contents.add(content);
    }


    /**
     * gets/sets
     */
    public List<JsonFolderContent> getContents() {
        return contents;
    }

    public void setContents(List<JsonFolderContent> contents) {
        this.contents = contents;
    }


}
